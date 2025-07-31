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
    @Pattern(regexp = "^[A-Za-z0-9 .#()-]+$", message = "Item name can only contain letters, numbers, spaces, and symbols (-.#())")    private String itemName;

    @Column(name = "status", nullable = false, length = 20)
    @NotBlank(message = "Status is required")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsibility_id", nullable = false, foreignKey = @ForeignKey(name = "FK_item_responsibility"))
    private Responsibility responsibility;

    // NEW: Add userId to track item ownership
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true, foreignKey = @ForeignKey(name = "FK_item_user"))
    private User user;

    // Constructor for creating items with name and status (original)
    public Item(String itemName, String status, Responsibility responsibility) {
        this.itemName = itemName;
        this.status = status;
        this.responsibility = responsibility;
        this.user = null; // No owner initially
    }

    // NEW: Constructor for creating items with owner
    public Item(String itemName, String status, Responsibility responsibility, User user) {
        this.itemName = itemName;
        this.status = status;
        this.responsibility = responsibility;
        this.user = user;
    }

    // Convenience method to get responsibility ID
    public Long getResponsibilityId() {
        return responsibility != null ? responsibility.getResponsibilityId() : null;
    }

    // Convenience method to get responsibility name
    public String getResponsibilityName() {
        return responsibility != null ? responsibility.getResponsibilityName() : null;
    }

    // NEW: Convenience method to get user ID
    public Long getUserId() {
        return user != null ? user.getUserId() : null;
    }

    // NEW: Convenience method to get user's full name
    public String getUserFullName() {
        if (user != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        return null;
    }

    // NEW: Check if item is owned by specific user
    public boolean isOwnedBy(Long userId) {
        return user != null && user.getUserId().equals(userId);
    }

    // NEW: Check if item is available (no owner)
    public boolean isAvailable() {
        return "Available".equals(status) && user == null;
    }

    // NEW: Check if item is in use (has owner)
    public boolean isInUse() {
        return "In Use".equals(status) && user != null;
    }
}