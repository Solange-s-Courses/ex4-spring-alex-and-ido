package com.project.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_responsibilities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponsibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = @ForeignKey(name = "FK_event_responsibility_event"))
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsibility_id", nullable = false, foreignKey = @ForeignKey(name = "FK_event_responsibility_responsibility"))
    private Responsibility responsibility;

    // Constructor for creating assignment
    public EventResponsibility(Event event, Responsibility responsibility) {
        this.event = event;
        this.responsibility = responsibility;
    }
}