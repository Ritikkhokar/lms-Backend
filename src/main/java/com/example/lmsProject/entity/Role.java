package com.example.lmsProject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter

@Setter
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roleId;

    @Column(name = "role_name", unique = true, nullable = false)
    private String roleName;

    private String description;
    // getters and setters
}

