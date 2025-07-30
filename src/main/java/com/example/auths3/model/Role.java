package com.example.auths3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name"))
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String description;
//CONSTRUCTORES
    public Role(){}
    public Role(String name){
        this.name=name;
    }
//GETTERS && SETTERS
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public String getName() {return name;}
    public void setDescription(String description) {this.description = description;}
    public String getDescription() {return description;}
}
