package com.example.auths3.dto;

import org.springframework.web.multipart.MultipartFile;
import java.util.Set;

public class SignupRequestDTO {
    private String name;
    private String email;
    private String password;
    private Set<String> roles = Set.of("ROLE_ADMIN");
    private MultipartFile profileImage;

    public void setName(String name) {this.name = name;}
    public String getName() {return name;}
    public void setEmail(String email) {this.email = email;}
    public String getEmail() {return email;}
    public void setPassword(String password) {this.password = password;}
    public String getPassword() {return password;}
    public void setRoles(Set<String> roles) {this.roles = roles;}
    public Set<String> getRoles() {return roles;}
    public void setProfileImage(MultipartFile profileImage) {this.profileImage = profileImage;}
    public MultipartFile getProfileImage() {return profileImage;}
}
