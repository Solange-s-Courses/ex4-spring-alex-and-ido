package com.project.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_name", nullable = false, length = 100)
    @NotBlank(message = "Event name is required")
    @Size(max = 100, message = "Event name cannot exceed 100 characters")
    private String eventName;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Column(name = "date_of_creation", nullable = false)
    @CreationTimestamp
    private LocalDateTime dateOfCreation;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // Constructor for creating event with name and description
    public Event(String eventName, String description) {
        this.eventName = eventName;
        this.description = description;
        this.status = "not-active"; // Default status
    }

    // Constructor for creating event with name only
    public Event(String eventName) {
        this.eventName = eventName;
        this.status = "not-active"; // Default status
    }

    // Status constants
    public static final String STATUS_NOT_ACTIVE = "not-active";
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_EQUIPMENT_RETURN = "equipment return";

    // Convenience method to check if event is ongoing (visible on dashboard)
    public boolean isOngoing() {
        return STATUS_ACTIVE.equals(status) || STATUS_EQUIPMENT_RETURN.equals(status);
    }

    // Convenience method to get formatted status for display
    public String getDisplayStatus() {
        return status.toUpperCase().replace("-", " ");
    }
}