package com.project.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email_address", unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String emailAddress;

    @Column(name = "phone_number", unique = true, nullable = false)
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @Column(name = "first_name", nullable = false, length = 20)
    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z]+$", message = "First name can only contain letters")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 20)
    @NotBlank(message = "Last name is required")
    @Size(max = 20, message = "Last name cannot exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z]+$", message = "Last name can only contain letters")
    private String lastName;

    // STEP 2 FIX: Updated password validation for BCrypt compatibility
    @Column(name = "encrypted_password", nullable = false, length = 255)
    @NotBlank(message = "Password is required")
    // Remove size and pattern constraints for database storage (validation handled in service layer)
    private String password;

    @Column(name = "date_of_issue", nullable = false)
    @CreationTimestamp
    private LocalDateTime dateOfIssue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "FK_user_role"))
    private Role role;

    // Convenience method to get role name
    public String getRoleName() {
        return role != null ? role.getName() : null;
    }

    @Transient
    private String responsibilityName;

    // Convenience method to check if user has specific role
    public boolean hasRole(String roleName) {
        return role != null && role.getName().equals(roleName);
    }

    // Method to get display role with responsibility
    public String getDisplayRole() {
        if ("manager".equals(getRoleName()) && responsibilityName != null) {
            return "MANAGER (" + responsibilityName + ")";
        }
        return getRoleName().toUpperCase();
    }
}