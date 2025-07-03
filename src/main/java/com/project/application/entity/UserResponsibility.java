package com.project.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_responsibilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponsibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_responsibility_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsibility_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_responsibility_responsibility"))
    private Responsibility responsibility;

    // Constructor for creating assignment
    public UserResponsibility(User user, Responsibility responsibility) {
        this.user = user;
        this.responsibility = responsibility;
    }
}