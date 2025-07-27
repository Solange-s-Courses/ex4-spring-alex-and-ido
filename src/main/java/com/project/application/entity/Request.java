package com.project.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "FK_request_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "FK_request_item"))
    private Item item;

    @Column(name = "request_type", nullable = false, length = 10)
    @NotBlank(message = "Request type is required")
    @Pattern(regexp = "^(request|return)$", message = "Request type must be 'request' or 'return'")
    private String requestType;

    @Column(name = "date_of_issue", nullable = false)
    @CreationTimestamp
    private LocalDateTime dateOfIssue;

    // Constructor for creating requests
    public Request(User user, Item item, String requestType) {
        this.user = user;
        this.item = item;
        this.requestType = requestType;
    }

    // Convenience method to get user ID
    public Long getUserId() {
        return user != null ? user.getUserId() : null;
    }

    // Convenience method to get item ID
    public Long getItemId() {
        return item != null ? item.getItemId() : null;
    }

    // Convenience method to get user's full name
    public String getUserFullName() {
        if (user != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        return null;
    }

    // Convenience method to get item name
    public String getItemName() {
        return item != null ? item.getItemName() : null;
    }

    // Convenience method to get responsibility ID
    public Long getResponsibilityId() {
        return item != null && item.getResponsibility() != null ?
                item.getResponsibility().getResponsibilityId() : null;
    }

    // Convenience method to get responsibility name
    public String getResponsibilityName() {
        return item != null && item.getResponsibility() != null ?
                item.getResponsibility().getResponsibilityName() : null;
    }
}