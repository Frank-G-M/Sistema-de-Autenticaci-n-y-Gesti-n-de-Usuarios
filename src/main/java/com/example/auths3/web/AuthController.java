package com.example.auths3.web;

import com.example.auths3.model.Role;
import com.example.auths3.model.User;
import com.example.auths3.repository.RepositoryUser;
import com.example.auths3.repository.RoleRepository;
import com.example.auths3.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final RepositoryUser repositoryUser;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthController(AuthenticationManager authManager, JwtTokenProvider tokenProvider, RepositoryUser repositoryUser, RoleRepository roleRepository, PasswordEncoder passwordEncoder){
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.repositoryUser = repositoryUser;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?>login(@RequestBody LoginRequest req){
        try{
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
            String token = tokenProvider.generateToken(req.email());
            return ResponseEntity.ok(new LoginResponse(token));
        }catch (AuthenticationException e){
            return ResponseEntity.status(401).body("Credenciales invalidas");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?>signup(@RequestBody SignupRequest signupRequest){
        if (repositoryUser.existsByEmail(signupRequest.email())){
            return ResponseEntity.badRequest().body("Error: Email ya esta en uso");
        }

        User user = new User(signupRequest.name(),signupRequest.email(),passwordEncoder.encode(signupRequest.password()));

        Set<Role> roles = new HashSet<>();

        if(signupRequest.roles()==null || signupRequest.roles().isEmpty()){
            signupRequest= new SignupRequest(
                    signupRequest.name(),
                    signupRequest.email(),
                    signupRequest.password(),
                    Set.of("USER")
            );
        }

        signupRequest.roles().forEach(role -> {
            Role userRole = roleRepository.findByName(role)
                    .orElseThrow(() -> new RuntimeException("Error: Rol '" + role + "' no encontrado"));
            roles.add(userRole);
        });
        user.setRoles(roles);
        repositoryUser.save(user);
        return ResponseEntity.ok(Map.of("message", "Usuario registrado exitosamente","email",user.getEmail()));
    }
}
record LoginRequest(String email, String password) {}
record LoginResponse(String token) {}
record SignupRequest(String name, String email, String password, Set<String> roles){ public  SignupRequest{roles = roles!=null? roles:Set.of("USER");}}