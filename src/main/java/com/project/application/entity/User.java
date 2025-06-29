package com.project.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

// Entity - Tells JPA this class represents a database table.
// Table - Specifies the actual table name in database (without this, it would use class name "User")
// Data - Automatically generates getters, setters, toString(), equals(), hashCode()
// NoArgsConstructor - Creates empty constructor User()
// AllArgsConstructor - Creates constructor with all fields User(userId, email, phone, password, date)

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    // id - Marks this as the primary key
    // GeneratedValue - Database auto-generates the ID (auto-increment)
    // column - Maps to database column "user_id"
    // unique - Creates unique constraint (no duplicate emails/phones allowed)
    // nullable - Field cannot be null (required field)
    // name - Maps Java camelCase to database snake_case
    // CreationTimeStamp - Hibernate automatically sets this to current timestamp when entity is first saved

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

    @Column(name = "encrypted_password", nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters")
    @Pattern(regexp = "^[A-Za-z0-9@$!%*?&]+$", message = "Password can only contain letters, numbers, and special characters (@$!%*?&)")
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

    // Convenience method to check if user has specific role
    public boolean hasRole(String roleName) {
        return role != null && role.getName().equals(roleName);
    }
}