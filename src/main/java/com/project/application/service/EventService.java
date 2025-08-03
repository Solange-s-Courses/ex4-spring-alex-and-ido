package com.project.application.service;

import com.project.application.entity.Event;
import com.project.application.repository.EventRepository;
import com.project.application.entity.Responsibility;
import com.project.application.repository.RequestRepository;
import com.project.application.repository.ResponsibilityRepository;
import com.project.application.entity.EventResponsibility;
import com.project.application.repository.EventResponsibilityRepository;

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
    private final EventResponsibilityRepository eventResponsibilityRepository;
    private final ItemService itemService;
    private final RequestRepository requestRepository;

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
     * Update event name and description (Chief only, not-active events only)
     */
    @Transactional
    public String updateEvent(Long eventId, String eventName, String description) {
        // Find the event
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (!eventOptional.isPresent()) {
            return "Event not found";
        }

        Event event = eventOptional.get();

        // Check if event is in not-active status (only not-active events can be edited)
        if (!Event.STATUS_NOT_ACTIVE.equals(event.getStatus())) {
            return "Only not-active events can be edited";
        }

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
            if (description.length() > 200) {
                return "Description cannot exceed 200 characters";
            }
            // If description is empty, set to null
            if (description.isEmpty()) {
                description = null;
            }
        }

        // Check if new event name already exists (case insensitive) and it's not the same event
        if (!event.getEventName().equalsIgnoreCase(eventName) &&
                eventRepository.existsByEventNameIgnoreCase(eventName)) {
            return "An event with this name already exists";
        }

        try {
            // Update the event
            event.setEventName(eventName);
            event.setDescription(description);
            eventRepository.save(event);
            return "success";
        } catch (Exception e) {
            return "Failed to update event: " + e.getMessage();
        }
    }

    /**
     * Activate event (Chief only, not-active events only)
     * Event must have at least one responsibility to be activated
     */
    @Transactional
    public String activateEvent(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (!eventOptional.isPresent()) {
            return "Event not found";
        }

        Event event = eventOptional.get();

        // Check if event is in not-active status
        if (!Event.STATUS_NOT_ACTIVE.equals(event.getStatus())) {
            return "Only not-active events can be activated";
        }

        // Check if event has at least one responsibility
        List<Responsibility> eventResponsibilities = getEventResponsibilities(eventId);
        if (eventResponsibilities.isEmpty()) {
            return "Event must have at least one responsibility before activation";
        }

        try {
            event.setStatus(Event.STATUS_ACTIVE);
            eventRepository.save(event);
            return "success";
        } catch (Exception e) {
            return "Failed to activate event: " + e.getMessage();
        }
    }

    /**
     * Switch event to return mode (Chief only, active events only)
     * UPDATED: Now clears all "request" type requests from event responsibilities
     */
    @Transactional
    public String switchToReturnMode(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (!eventOptional.isPresent()) {
            return "Event not found";
        }

        Event event = eventOptional.get();

        // Check if event is in active status
        if (!Event.STATUS_ACTIVE.equals(event.getStatus())) {
            return "Only active events can be switched to return mode";
        }

        try {
            // NEW: Clear all "request" type requests from responsibilities connected to this event
            List<Responsibility> eventResponsibilities = getEventResponsibilities(eventId);
            if (!eventResponsibilities.isEmpty()) {
                // Clear only "request" type requests for each responsibility
                for (Responsibility responsibility : eventResponsibilities) {
                    requestRepository.deleteByResponsibilityIdAndRequestType(
                            responsibility.getResponsibilityId(), "request");
                }
            }

            // Switch event status to equipment return
            event.setStatus(Event.STATUS_EQUIPMENT_RETURN);
            eventRepository.save(event);
            return "success";
        } catch (Exception e) {
            return "Failed to switch to return mode: " + e.getMessage();
        }
    }


    /**
     * Switch event back to active mode (Chief only, equipment return events only)
     */
    @Transactional
    public String switchToActiveMode(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (!eventOptional.isPresent()) {
            return "Event not found";
        }

        Event event = eventOptional.get();

        // Check if event is in equipment return status
        if (!Event.STATUS_EQUIPMENT_RETURN.equals(event.getStatus())) {
            return "Only events in equipment return mode can be switched back to active";
        }

        try {
            event.setStatus(Event.STATUS_ACTIVE);
            eventRepository.save(event);
            return "success";
        } catch (Exception e) {
            return "Failed to switch to active mode: " + e.getMessage();
        }
    }

    /**
     * Complete event (Chief only, equipment return events only)
     * UPDATED: Now checks for "In Use" items before allowing completion
     * Returns event to not-active status for potential reuse
     */
    @Transactional
    public String completeEvent(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);

        if (!eventOptional.isPresent()) {
            return "Event not found";
        }

        Event event = eventOptional.get();

        // Check if event is in equipment return status
        if (!Event.STATUS_EQUIPMENT_RETURN.equals(event.getStatus())) {
            return "Only events in equipment return mode can be completed";
        }

        // NEW: Check for items still in use before allowing completion
        String inUseValidation = validateNoItemsInUse(eventId);
        if (!"success".equals(inUseValidation)) {
            return inUseValidation;
        }

        try {
            event.setStatus(Event.STATUS_NOT_ACTIVE);
            eventRepository.save(event);
            return "success";
        } catch (Exception e) {
            return "Failed to complete event: " + e.getMessage();
        }
    }

    /**
     * NEW: Validate that no items are still "In Use" for any responsibility in the event
     */
    private String validateNoItemsInUse(Long eventId) {
        try {
            // Get all responsibilities assigned to this event
            List<Responsibility> eventResponsibilities = getEventResponsibilities(eventId);

            if (eventResponsibilities.isEmpty()) {
                return "success"; // No responsibilities, no items to check
            }

            // Check each responsibility for "In Use" items
            for (Responsibility responsibility : eventResponsibilities) {
                List<com.project.application.entity.Item> inUseItems = itemService.getItemsByResponsibilityIdAndStatus(
                        responsibility.getResponsibilityId(), "In Use");

                if (!inUseItems.isEmpty()) {
                    // Found items still in use
                    return String.format("Cannot complete event: %d item(s) still in use in responsibility '%s'. All items must be returned before completing the event.",
                            inUseItems.size(), responsibility.getResponsibilityName());
                }
            }

            return "success"; // No items in use found

        } catch (Exception e) {
            return "Failed to validate items status: " + e.getMessage();
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
            // First, delete all event-responsibility relationships for this event
            eventResponsibilityRepository.deleteByEventEventId(eventId);

            // Then delete the event itself
            eventRepository.delete(event);
            return "success";
        } catch (Exception e) {
            return "Failed to delete event: " + e.getMessage();
        }
    }

    /**
     * Add responsibility to event (UPDATED - allows same responsibility in multiple events)
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

        // Check if responsibility is already assigned to THIS specific event
        if (eventResponsibilityRepository.existsByEventEventIdAndResponsibilityResponsibilityId(eventId, responsibilityId)) {
            return "Responsibility is already assigned to this event";
        }

        try {
            // Create new assignment
            Event event = eventOptional.get();
            Responsibility responsibility = responsibilityOptional.get();
            EventResponsibility eventResponsibility = new EventResponsibility(event, responsibility);
            eventResponsibilityRepository.save(eventResponsibility);
            return "success";
        } catch (Exception e) {
            return "Failed to add responsibility to event: " + e.getMessage();
        }
    }

    /**
     * Remove responsibility from event (UPDATED)
     */
    @Transactional
    public String removeResponsibilityFromEvent(Long eventId, Long responsibilityId) {
        // Check if assignment exists
        if (!eventResponsibilityRepository.existsByEventEventIdAndResponsibilityResponsibilityId(eventId, responsibilityId)) {
            return "Responsibility is not assigned to this event";
        }

        try {
            // Remove assignment
            eventResponsibilityRepository.deleteByEventEventIdAndResponsibilityResponsibilityId(eventId, responsibilityId);
            return "success";
        } catch (Exception e) {
            return "Failed to remove responsibility from event: " + e.getMessage();
        }
    }

    /**
     * Get responsibilities assigned to a specific event (UPDATED)
     */
    public List<Responsibility> getEventResponsibilities(Long eventId) {
        return eventResponsibilityRepository.findResponsibilitiesByEventId(eventId);
    }

    /**
     * Get responsibilities not assigned to a specific event (UPDATED)
     */
    public List<Responsibility> getUnassignedResponsibilities(Long eventId) {
        return eventResponsibilityRepository.findResponsibilitiesNotAssignedToEvent(eventId);
    }

    /**
     * Get total event count
     */
    public long getTotalEventCount() {
        return eventRepository.count();
    }

    /**
     * Check if any active events allow item requests
     * Returns true if there are events in "active" status
     */
    public boolean areItemRequestsAllowed() {
        List<Event> activeEvents = getEventsByStatus(Event.STATUS_ACTIVE);
        return !activeEvents.isEmpty();
    }

    /**
     * Check if any events allow item returns (updated to include active events)
     * Returns true if there are events in "active" OR "equipment return" status
     */
    public boolean areItemReturnsAllowed() {
        List<Event> activeEvents = getEventsByStatus(Event.STATUS_ACTIVE);
        List<Event> returnEvents = getEventsByStatus(Event.STATUS_EQUIPMENT_RETURN);
        return !activeEvents.isEmpty() || !returnEvents.isEmpty();
    }

    /**
     * Check if a specific responsibility is part of any active event (for requests)
     */
    public boolean isResponsibilityInActiveEvent(Long responsibilityId) {
        List<Event> activeEvents = getEventsByStatus(Event.STATUS_ACTIVE);
        for (Event event : activeEvents) {
            List<Responsibility> eventResponsibilities = getEventResponsibilities(event.getEventId());
            for (Responsibility responsibility : eventResponsibilities) {
                if (responsibility.getResponsibilityId().equals(responsibilityId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if a specific responsibility is part of any return-mode event (for returns)
     */
    public boolean isResponsibilityInReturnEvent(Long responsibilityId) {
        List<Event> returnEvents = getEventsByStatus(Event.STATUS_EQUIPMENT_RETURN);
        for (Event event : returnEvents) {
            List<Responsibility> eventResponsibilities = getEventResponsibilities(event.getEventId());
            for (Responsibility responsibility : eventResponsibilities) {
                if (responsibility.getResponsibilityId().equals(responsibilityId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if a specific responsibility is part of any event that allows returns
     * (active or equipment return events)
     */
    public boolean isResponsibilityInReturnAllowedEvent(Long responsibilityId) {
        // Get both active and equipment return events
        List<Event> activeEvents = getEventsByStatus(Event.STATUS_ACTIVE);
        List<Event> returnEvents = getEventsByStatus(Event.STATUS_EQUIPMENT_RETURN);

        // Check active events
        for (Event event : activeEvents) {
            List<Responsibility> eventResponsibilities = getEventResponsibilities(event.getEventId());
            for (Responsibility responsibility : eventResponsibilities) {
                if (responsibility.getResponsibilityId().equals(responsibilityId)) {
                    return true;
                }
            }
        }

        // Check equipment return events
        for (Event event : returnEvents) {
            List<Responsibility> eventResponsibilities = getEventResponsibilities(event.getEventId());
            for (Responsibility responsibility : eventResponsibilities) {
                if (responsibility.getResponsibilityId().equals(responsibilityId)) {
                    return true;
                }
            }
        }

        return false;
    }
}