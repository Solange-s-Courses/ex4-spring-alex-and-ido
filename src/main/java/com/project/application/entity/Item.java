package com.project.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_name", nullable = false, length = 32)
    @NotBlank(message = "Item name is required")
    @Size(max = 32, message = "Item name cannot exceed 32 characters")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Item name can only contain letters and spaces")
    private String itemName;

    @Column(name = "status", nullable = false, length = 20)
    @NotBlank(message = "Status is required")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsibility_id", nullable = false, foreignKey = @ForeignKey(name = "FK_item_responsibility"))
    private Responsibility responsibility;

    // Constructor for creating items with name and status
    public Item(String itemName, String status, Responsibility responsibility) {
        this.itemName = itemName;
        this.status = status;
        this.responsibility = responsibility;
    }

    // Convenience method to get responsibility ID
    public Long getResponsibilityId() {
        return responsibility != null ? responsibility.getResponsibilityId() : null;
    }

    // Convenience method to get responsibility name
    public String getResponsibilityName() {
        return responsibility != null ? responsibility.getResponsibilityName() : null;
    }
}