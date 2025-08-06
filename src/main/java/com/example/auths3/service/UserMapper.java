package com.example.auths3.service;

import com.example.auths3.dto.UserDTO;
import com.example.auths3.model.Role;
import com.example.auths3.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public static UserDTO userDTO(User user){
        List<String> roleName = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new UserDTO(user.getId(), user.getName(), user.getEmail(), roleName);
    }
}
