package com.project.application.service;

import com.project.application.entity.Event;
import com.project.application.repository.EventRepository;
import com.project.application.entity.Responsibility;
import com.project.application.repository.ResponsibilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ResponsibilityRepository responsibilityRepository;

    /**
     * Create a new event
     */
    @Transactional
    public String createEvent(String eventName, String description) {
        // Validate input
        if (eventName == null || eventName.trim().isEmpty()) {
            return "Event name is required";
        }

        eventName = eventName.trim();

        // Check length limits
        if (eventName.length() > 100) {
            return "Event name cannot exceed 100 characters";
        }

        if (description != null) {
            description = description.trim();
            if (description.length() > 500) {
                return "Description cannot exceed 500 characters";
            }
            // If description is empty, set to null
            if (description.isEmpty()) {
                description = null;
            }
        }

        // Check if event name already exists (case insensitive)
        if (eventRepository.existsByEventNameIgnoreCase(eventName)) {
            return "An event with this name already exists";
        }

        try {
            // Create new event
            Event event = new Event(eventName, description);
            eventRepository.save(event);
            return "success";
        } catch (Exception e) {
            return "Failed to create event: " + e.getMessage();
        }
    }

    /**
     * Get all events ordered by creation date (newest first)
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAllOrderByDateDesc();
    }

    /**
     * Get ongoing events only (active or equipment return)
     */
    public List<Event> getOngoingEvents() {
        return eventRepository.findOngoingEvents();
    }

    /**
     * Get event by ID
     */
    public Optional<Event> findById(Long eventId) {
        return eventRepository.findById(eventId);
    }

    /**
     * Get event by name
     */
    public Optional<Event> findByName(String eventName) {
        return eventRepository.findByEventName(eventName);
    }

    /**
     * Get events by status
     */
    public List<Event> getEventsByStatus(String status) {
        return eventRepository.findByStatus(status);
    }

    /**
     * Update event status (for future use)
     */
    @Transactional
    public String updateEventStatus(Long eventId, String newStatus) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (!eventOptional.isPresent()) {
            return "Event not found";
        }

        Event event = eventOptional.get();

        // Validate status
        if (!isValidStatus(newStatus)) {
            return "Invalid status. Valid statuses are: not-active, active, equipment return";
        }

        try {
            event.setStatus(newStatus);
            eventRepository.save(event);
            return "success";
        } catch (Exception e) {
            return "Failed to update event status: " + e.getMessage();
        }
    }

    /**
     * Check if status is valid
     */
    private boolean isValidStatus(String status) {
        return Event.STATUS_NOT_ACTIVE.equals(status) ||
                Event.STATUS_ACTIVE.equals(status) ||
                Event.STATUS_EQUIPMENT_RETURN.equals(status);
    }

    /**
     * Delete event and unassign responsibilities (Chief only, not-active events only)
     */
    @Transactional
    public String deleteEvent(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (!eventOptional.isPresent()) {
            return "Event not found";
        }

        Event event = eventOptional.get();

        // Check if event is in not-active status
        if (!Event.STATUS_NOT_ACTIVE.equals(event.getStatus())) {
            return "Only not-active events can be deleted";
        }

        try {
            // Note: Responsibilities will be automatically unassigned due to the foreign key relationship
            // When the event is deleted, the event_id in responsibilities table will be set to NULL
            // (assuming the foreign key is set up with ON DELETE SET NULL)
            eventRepository.delete(event);
            return "success";
        } catch (Exception e) {
            return "Failed to delete event: " + e.getMessage();
        }
    }

    /**
     * Add responsibility to event
     */
    @Transactional
    public String addResponsibilityToEvent(Long eventId, Long responsibilityId) {
        // Find event
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if (!eventOptional.isPresent()) {
            return "Event not found";
        }

        // Find responsibility
        Optional<Responsibility> responsibilityOptional = responsibilityRepository.findById(responsibilityId);
        if (!responsibilityOptional.isPresent()) {
            return "Responsibility not found";
        }

        Event event = eventOptional.get();
        Responsibility responsibility = responsibilityOptional.get();

        // Check if responsibility is already assigned to another event
        if (responsibility.getEvent() != null) {
            return "Responsibility is already assigned to another event";
        }

        try {
            // Assign responsibility to event
            responsibility.setEvent(event);
            responsibilityRepository.save(responsibility);
            return "success";
        } catch (Exception e) {
            return "Failed to add responsibility to event: " + e.getMessage();
        }
    }

    /**
     * Remove responsibility from event
     */
    @Transactional
    public String removeResponsibilityFromEvent(Long eventId, Long responsibilityId) {
        // Find responsibility
        Optional<Responsibility> responsibilityOptional = responsibilityRepository.findById(responsibilityId);
        if (!responsibilityOptional.isPresent()) {
            return "Responsibility not found";
        }

        Responsibility responsibility = responsibilityOptional.get();

        // Check if responsibility belongs to the specified event
        if (responsibility.getEvent() == null || !responsibility.getEvent().getEventId().equals(eventId)) {
            return "Responsibility is not assigned to this event";
        }

        try {
            // Remove responsibility from event
            responsibility.setEvent(null);
            responsibilityRepository.save(responsibility);
            return "success";
        } catch (Exception e) {
            return "Failed to remove responsibility from event: " + e.getMessage();
        }
    }

    /**
     * Get responsibilities assigned to a specific event
     */
    public List<Responsibility> getEventResponsibilities(Long eventId) {
        return responsibilityRepository.findByEventEventId(eventId);
    }

    /**
     * Get responsibilities not assigned to any event
     */
    public List<Responsibility> getUnassignedResponsibilities() {
        return responsibilityRepository.findByEventIsNull();
    }

    /**
     * Get total event count
     */
    public long getTotalEventCount() {
        return eventRepository.count();
    }
}