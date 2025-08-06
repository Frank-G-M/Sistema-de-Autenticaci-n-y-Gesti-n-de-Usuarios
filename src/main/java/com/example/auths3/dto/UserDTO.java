package com.example.auths3.dto;

import java.util.List;

public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private List<String> role;

public UserDTO(){}

public UserDTO(Long id, String name, String email, List<String> role){
    this.id = id;
    this.name = name;
    this.email = email;
    this.role = role;
}
    //GETTERS & SETTERS
    public void setId(Long id) {this.id = id;}
    public Long getId() {return id;}
    public void setName(String name) {this.name = name;}
    public String getName() {return name;}
    public void setEmail(String email) {this.email = email;}
    public String getEmail() {return email;}
    public void setRole(List<String> role) {this.role = role;}
    public List<String> getRole() {return role;}
}

