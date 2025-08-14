package com.example.auths3.controller;

import com.example.auths3.dto.*;
import com.example.auths3.model.Role;
import com.example.auths3.model.User;
import com.example.auths3.repository.RepositoryUser;
import com.example.auths3.repository.RoleRepository;
import com.example.auths3.security.JwtTokenProvider;
import com.example.auths3.service.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final RepositoryUser repositoryUser;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider tokenProvider, RepositoryUser repositoryUser, RoleRepository roleRepository, PasswordEncoder passwordEncoder){
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.repositoryUser = repositoryUser;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?>login(@RequestBody LoginRequestDTO req){
        try{
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
            String token = tokenProvider.generateToken(req.getEmail());
            return ResponseEntity.ok(new LoginResponse(token));
        }catch (AuthenticationException e){
            return ResponseEntity.status(401).body("Credenciales invalidas");
        }
    }

    @PostMapping(value = "/auth/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>>signup(
            @RequestPart("userData") String userDataStr,
            @RequestPart(value = "profileImage", required = false)MultipartFile file) {
        System.out.println("Bucktname name: "+bucketName);
        ObjectMapper mapper = new ObjectMapper();
        SignupRequestDTO signupRequest;
        try {
            signupRequest = mapper.readValue(userDataStr, SignupRequestDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Formato JSON inválido", null));
        }
        try {
            s3Client.headBucket(b -> b.bucket(bucketName));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "Error con S3: " + e.getMessage(), null));
        }

        if (repositoryUser.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "El email ya está registrado", null));
        }
        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));
        if (file != null && !file.isEmpty()){
            user.setProfileImageUrl(uploadImageToS3(file));
        }
        Set<Role>roles=signupRequest.getRoles().stream()
                .map(roleName-> {
                            String fullRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                            return roleRepository.findByName(fullRoleName).orElseThrow(() -> new RuntimeException("ROL no encontrado: " + fullRoleName));
                        }).collect(Collectors.toSet());
        user.setRoles(roles);
        User savedUser=repositoryUser.save(user);
        UserDTO response = new UserDTO();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setProfileImage(savedUser.getProfileImageUrl());
        response.setRole(savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Registro exitoso", response));
    }
    private String uploadImageToS3(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            software.amazon.awssdk.core.sync.RequestBody requestBody =
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    );
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(putObjectRequest,requestBody);


            return s3Client.utilities().getUrl(builder -> builder
                    .bucket(bucketName)
                    .key(fileName)
            ).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la imagen"+e.getMessage(),e);
        }
    }

    @GetMapping("/auth/me")
    public ResponseEntity<UserDTO>getProfile(@AuthenticationPrincipal UserDetails userDetails){
        String email = userDetails.getUsername();
        User user = repositoryUser.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Usuario existente"));
        UserDTO dto = UserMapper.userDTO(user);
        return ResponseEntity.ok(dto);
    }

    @PutMapping(value = "/users/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?>updateProfilePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("profileImage")MultipartFile file){
        try {
            String email = userDetails.getUsername();
            User user = repositoryUser.findByEmail(email).orElseThrow(()->new RuntimeException("Usuario no encontrado"));

            String newImageUrl = uploadImageToS3(file);

            if(user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()){
                deleteImageFromS3(user.getProfileImageUrl());
            }
            user.setProfileImageUrl(newImageUrl);
            repositoryUser.save(user);

            UserDTO response = new UserDTO();
            response.setId(user.getId());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setProfileImage(user.getProfileImageUrl());
            response.setRole(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));

            return ResponseEntity.ok(new ApiResponse<>(true, "Foto actualizada",response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false,"Error en actualizar imagen: "+e.getMessage(),null));
        }
    }
    private void deleteImageFromS3(String imageUrl){
        try {
            String key = imageUrl.substring(imageUrl.lastIndexOf("/")+1);
            s3Client.deleteObject(builder -> builder
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (Exception e) {
            System.err.println("Error al eliminar imagen: "+e.getMessage());
        }
    }


}
record LoginRequest(String email, String password) {}
record LoginResponse(String token) {}
record SignupRequest(String name, String email, String password, Set<String> roles, MultipartFile profileImage){ public  SignupRequest{roles = roles!=null? roles:Set.of("ROLE_USER");}}