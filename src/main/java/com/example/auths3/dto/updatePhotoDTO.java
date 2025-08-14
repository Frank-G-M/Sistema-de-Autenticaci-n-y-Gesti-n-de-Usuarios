package com.example.auths3.dto;

import org.springframework.web.multipart.MultipartFile;

public class updatePhotoDTO {
    private MultipartFile profileImage;

    public MultipartFile getProfileImage(){return profileImage;}
    public void setProfileImage(MultipartFile profileImage) {this.profileImage = profileImage;}
}
