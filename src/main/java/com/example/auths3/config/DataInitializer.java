package com.example.auths3.config;

import com.example.auths3.model.Role;
import com.example.auths3.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;

    public DataInitializer (RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            Role userRole = new Role("ROLE_USER");
            userRole.setDescription("Rol General");
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            Role adminRole = new Role("ROLE_ADMIN");
            adminRole.setDescription("ROL ADMINISTRATIVO");
            roleRepository.save(adminRole);
        }
    }
}
