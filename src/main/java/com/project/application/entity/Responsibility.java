package com.project.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "responsibilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Responsibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "responsibility_id")
    private Long responsibilityId;

    @Column(name = "responsibility_name", nullable = false, length = 100)
    private String responsibilityName;

    @Column(name = "description", length = 500)
    private String description;

    // Constructor for creating responsibility with name only
    public Responsibility(String responsibilityName) {
        this.responsibilityName = responsibilityName;
    }

    // Constructor for creating responsibility with name and description
    public Responsibility(String responsibilityName, String description) {
        this.responsibilityName = responsibilityName;
        this.description = description;
    }
}