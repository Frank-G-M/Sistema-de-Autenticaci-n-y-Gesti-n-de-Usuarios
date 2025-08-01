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
    public void init(){
        if (roleRepository.findByName("USER").isEmpty()){
            Role userRole = new Role("USER");
            userRole.setDescription("Rol General");
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName("ADMIN").isEmpty()){
            Role adminRole = new Role("ADMIN");
            adminRole.setDescription("ROL ADMINISTRATIVO");
            roleRepository.save(adminRole);
        }
}
}
