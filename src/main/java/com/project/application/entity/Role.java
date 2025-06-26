package com.project.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Column(name = "name", nullable = false, unique = true, length = 20)
    private String name;

    // Constructor for creating roles with name only (roleId will be auto-generated)
    public Role(String name) {
        this.name = name;
    }
}