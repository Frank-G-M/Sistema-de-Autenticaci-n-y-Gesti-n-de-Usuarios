package com.example.auths3.service;

import com.example.auths3.model.Role;
import com.example.auths3.model.User;
import com.example.auths3.repository.RepositoryUser;
import com.example.auths3.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final RepositoryUser repositoryUser;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(RepositoryUser repositoryUser, PasswordEncoder passwordEncoder, RoleRepository roleRepository){
        this.repositoryUser = repositoryUser;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public User registerUser(String name, String email, String rawPassword) {
        if (repositoryUser.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está en uso");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(()->new IllegalStateException("Falta cargar ROLE_USER"));
        user.addRole(roleUser);
        return repositoryUser.save(user);
    }
}