package com.project.application.repository;

import com.project.application.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find event by name
    Optional<Event> findByEventName(String eventName);

    // Find all ongoing events (active or equipment return status)
    @Query("SELECT e FROM Event e WHERE e.status = 'active' OR e.status = 'equipment return'")
    List<Event> findOngoingEvents();

    // Find all events ordered by creation date (newest first)
    @Query("SELECT e FROM Event e ORDER BY e.dateOfCreation DESC")
    List<Event> findAllOrderByDateDesc();

    // Find events by status
    List<Event> findByStatus(String status);

    // Check if event name already exists (case insensitive)
    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE LOWER(e.eventName) = LOWER(?1)")
    boolean existsByEventNameIgnoreCase(String eventName);
}