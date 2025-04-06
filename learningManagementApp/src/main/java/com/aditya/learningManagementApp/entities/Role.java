package com.aditya.learningManagementApp.entities;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) // Ensured unique role name
    private String name;

    //  Default constructor for JPA
    public Role() {
    }

    //  Constructor for easier role creation
    public Role(String name) {
        this.name = name;
    }

    //  Implemented method from GrantedAuthority
    @Override
    public String getAuthority() {
        return name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
