package com.example.auths3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

//ATRIBUTOS CON JPA
@Entity
@Table(name="users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String profileImageUrl;

    @NotBlank
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"), uniqueConstraints = @UniqueConstraint(name = "uk_users_roles_user_role", columnNames = {"user_id", "role_id"}))
    private Set<Role> roles = new HashSet<>();

// CONSTRUCTORES
    public User(){}
    public User(String name, String email, String passwordHash){
        this.name=name;
        this.email = email;
        this.passwordHash= passwordHash;
    }
    // HELPERS
    public void addRole(Role role){this.roles.add(role);}
    public void removeRole(Role role){this.roles.remove(role);}

//GETTERS & SETTERS
    public void setId(Long id) {this.id = id;}
    public Long getId() {return id;}
    public void setName(String name) {this.name = name;}
    public String getName() {return name;}
    public void setEmail(String email) {this.email = email;}
    public String getEmail() {return email;}
    public void setProfileImageUrl(String profileImageUrl) {this.profileImageUrl = profileImageUrl;}
    public String getProfileImageUrl() {return profileImageUrl;}
    public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;}
    public String getPasswordHash() {return passwordHash;}
    public void setRoles(Set<Role> roles) {this.roles = roles;}
    public Set<Role> getRoles() {return roles;}
}