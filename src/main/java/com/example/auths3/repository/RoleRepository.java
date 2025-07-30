package com.example.auths3.repository;

import com.example.auths3.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role>findByName(String name);
}