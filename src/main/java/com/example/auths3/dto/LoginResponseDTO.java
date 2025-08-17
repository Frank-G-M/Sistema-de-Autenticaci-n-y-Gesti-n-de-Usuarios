package com.example.auths3.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    public LoginResponseDTO(String token){
        this.token = token;
    }
}