package com.example.auths3.controller;

import com.example.auths3.dto.UserDTO;
import com.example.auths3.model.Role;
import com.example.auths3.model.User;
import com.example.auths3.repository.RepositoryUser;
import com.example.auths3.repository.RoleRepository;
import com.example.auths3.security.JwtTokenProvider;
import com.example.auths3.service.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
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

    @PostMapping("/login")
    public ResponseEntity<?>login(@org.springframework.web.bind.annotation.RequestBody LoginRequest req){
        try{
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
            String token = tokenProvider.generateToken(req.email());
            return ResponseEntity.ok(new LoginResponse(token));
        }catch (AuthenticationException e){
            return ResponseEntity.status(401).body("Credenciales invalidas");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?>signup(@RequestPart("userData") SignupRequest signupRequest, @RequestPart(value = "profileImage", required = false)MultipartFile file) {

        if (repositoryUser.existsByEmail(signupRequest.email())) {
            return ResponseEntity.badRequest().body("Error: Email ya esta en uso");
        }
        SignupRequest updatedRequest = file != null ?
                new SignupRequest(
                        signupRequest.name(),
                        signupRequest.email(),
                        signupRequest.password(),
                        signupRequest.roles(),
                        file
                ) : signupRequest;

        User user = new User(signupRequest.name(), signupRequest.email(), passwordEncoder.encode(signupRequest.password()));

        if (updatedRequest.profileImage() != null && !updatedRequest.profileImage().isEmpty()) {
            String imageUrl = uploadImageToS3(updatedRequest.profileImage());
            user.setProfileImageUrl(imageUrl);
        }

        Set<Role> roles = new HashSet<>();
        Set<String> requestRoles = updatedRequest.roles() != null ?
                updatedRequest.roles() : Set.of("USER");

        requestRoles.forEach(role -> {
            Role userRole = roleRepository.findByName(role).orElseThrow(() -> new RuntimeException("Error: Rol '" + role + "' no encontrado"));
            roles.add(userRole);
        });
        user.setRoles(roles);
        repositoryUser.save(user);

        return ResponseEntity.ok(Map.of("message", "Usuario registrado correctamente", "email", user.getEmail(), "imageUrl", user.getProfileImageUrl()));
    }
    private String uploadImageToS3(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            // Versión más explícita
            software.amazon.awssdk.core.sync.RequestBody requestBody =
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    );

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .acl(ObjectCannedACL.PUBLIC_READ)
                            .build(),
                    requestBody
            );

            return s3Client.utilities().getUrl(builder -> builder
                    .bucket(bucketName)
                    .key(fileName)
            ).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la imagen", e);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO>getProfile(@AuthenticationPrincipal UserDetails userDetails){
        String email = userDetails.getUsername();
        User user = repositoryUser.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Usuario existente"));
        UserDTO dto = UserMapper.userDTO(user);
        return ResponseEntity.ok(dto);
    }

}
record LoginRequest(String email, String password) {}
record LoginResponse(String token) {}
record SignupRequest(String name, String email, String password, Set<String> roles, MultipartFile profileImage){ public  SignupRequest{roles = roles!=null? roles:Set.of("USER");}}