package com.example.auths3.repository;

import com.example.auths3.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RepositoryUser extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);
}