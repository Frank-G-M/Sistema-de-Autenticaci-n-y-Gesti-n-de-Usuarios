package com.example.auths3.controller;

import com.example.auths3.dto.UserDTO;
import com.example.auths3.repository.RepositoryUser;
import com.example.auths3.service.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RepositoryUser repositoryUser;

    public AdminController (RepositoryUser repositoryUser){
        this.repositoryUser = repositoryUser;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<List<UserDTO>>getAllUsers(){
        List<UserDTO> users = repositoryUser.findAll()
                .stream()
                .map(UserMapper::userDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

}
//Este controlador expone este end-point GET /api/admin/users, devolviendo usuarios en formato de UserDTO accediendo solo el ROL ADMIN en respuesta JSON