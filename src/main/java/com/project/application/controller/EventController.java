package com.project.application.controller;

import com.project.application.controller.helper.SecurityHelper;
import com.project.application.entity.Event;
import com.project.application.entity.Responsibility;
import com.project.application.entity.User;
import com.project.application.service.EventService;
import com.project.application.service.ResponsibilityService;
import com.project.application.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Controller handling all event-related operations
 * STEP 4: Updated to use Spring Security instead of manual session management
 */
@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final ResponsibilityService responsibilityService;
    private final UserService userService;
    private final SecurityHelper securityHelper;

    // ==========================================
    // EVENT CRUD OPERATIONS (CHIEF ONLY)
    // ==========================================

    /**
     * Create new event (Chief only)
     */
    @PostMapping("/chief/events/create")
    @PreAuthorize("hasRole('CHIEF')")
    public String createEvent(@RequestParam String eventName,
                              @RequestParam(required = false) String description,
                              RedirectAttributes redirectAttributes,
                              Model model) throws UnsupportedEncodingException {

        User user = securityHelper.getCurrentUser();

        // Create event using service
        String result = eventService.createEvent(eventName, description);

        if ("success".equals(result)) {
            return "redirect:/dashboard?success=" + java.net.URLEncoder.encode("Event created successfully!", "UTF-8");
        } else {
            // Error occurred - reload dashboard with form data and error message

            // Get all responsibilities with their managers for the dashboard
            Map<Responsibility, List<User>> responsibilitiesWithManagers = responsibilityService.getAllResponsibilitiesWithManagers();

            // Get all events for chief
            List<Event> events = eventService.getAllEvents();

            // Add all necessary data to model
            model.addAttribute("user", user);
            model.addAttribute("userRole", user.getRoleName());
            model.addAttribute("responsibilitiesWithManagers", responsibilitiesWithManagers);
            model.addAttribute("events", events);

            // Add form data to preserve user input
            model.addAttribute("eventFormData", new EventFormData(eventName, description));

            // Add error message for the form
            model.addAttribute("eventFormError", result);

            // Add flag to show the form
            model.addAttribute("showEventForm", true);

            return "dashboard";
        }
    }

    // Helper class for form data
    @Getter
    public static class EventFormData {
        private final String eventName;
        private final String description;

        public EventFormData(String eventName, String description) {
            this.eventName = eventName;
            this.description = description;
        }
    }

    /**
     * Delete event (Chief only, not-active events only)
     */
    @PostMapping("/chief/events/delete")
    @PreAuthorize("hasRole('CHIEF')")
    public String deleteEvent(@RequestParam Long eventId,
                              RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {

        // Delete event using service
        String result = eventService.deleteEvent(eventId);

        if ("success".equals(result)) {
            return "redirect:/dashboard?success=" + java.net.URLEncoder.encode("Event deleted successfully!", "UTF-8");
        } else {
            return "redirect:/dashboard?error=" + java.net.URLEncoder.encode(result, "UTF-8");
        }
    }

    /**
     * Edit event (Chief only, not-active events only)
     */
    @PostMapping("/chief/events/{eventId}/edit")
    @ResponseBody
    @PreAuthorize("hasRole('CHIEF')")
    public Map<String, Object> editEvent(@PathVariable Long eventId,
                                         @RequestParam String eventName,
                                         @RequestParam(required = false) String description) {
        Map<String, Object> response = new HashMap<>();

        // Update event using service
        String result = eventService.updateEvent(eventId, eventName, description);

        if ("success".equals(result)) {
            response.put("success", true);
            response.put("message", "Event updated successfully");
        } else {
            response.put("success", false);
            response.put("message", result);
        }

        return response;
    }

    // ==========================================
    // EVENT LIFECYCLE MANAGEMENT (CHIEF ONLY)
    // ==========================================

    /**
     * Activate event (Chief only, not-active events only)
     */
    @PostMapping("/chief/events/{eventId}/activate")
    @ResponseBody
    @PreAuthorize("hasRole('CHIEF')")
    public Map<String, Object> activateEvent(@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();

        // Activate event using service
        String result = eventService.activateEvent(eventId);

        if ("success".equals(result)) {
            response.put("success", true);
            response.put("message", "Event activated successfully");
        } else {
            response.put("success", false);
            response.put("message", result);
        }

        return response;
    }

    /**
     * Switch event to return mode (Chief only, active events only)
     */
    @PostMapping("/chief/events/{eventId}/switch-to-return")
    @ResponseBody
    @PreAuthorize("hasRole('CHIEF')")
    public Map<String, Object> switchToReturnMode(@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();

        // Switch to return mode using service
        String result = eventService.switchToReturnMode(eventId);

        if ("success".equals(result)) {
            response.put("success", true);
            response.put("message", "Event switched to return mode successfully");
        } else {
            response.put("success", false);
            response.put("message", result);
        }

        return response;
    }

    /**
     * Switch event back to active mode (Chief only, equipment return events only)
     */
    @PostMapping("/chief/events/{eventId}/switch-to-active")
    @ResponseBody
    @PreAuthorize("hasRole('CHIEF')")
    public Map<String, Object> switchToActiveMode(@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();

        // Switch to active mode using service
        String result = eventService.switchToActiveMode(eventId);

        if ("success".equals(result)) {
            response.put("success", true);
            response.put("message", "Event switched to active mode successfully");
        } else {
            response.put("success", false);
            response.put("message", result);
        }

        return response;
    }

    /**
     * Complete event (Chief only, equipment return events only)
     */
    @PostMapping("/chief/events/{eventId}/complete")
    @ResponseBody
    @PreAuthorize("hasRole('CHIEF')")
    public Map<String, Object> completeEvent(@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();

        // Complete event using service
        String result = eventService.completeEvent(eventId);

        if ("success".equals(result)) {
            response.put("success", true);
            response.put("message", "Event completed successfully");
        } else {
            response.put("success", false);
            response.put("message", result);
        }

        return response;
    }

    // ==========================================
    // EVENT VIEWING
    // ==========================================

    /**
     * Display event details page for authorized users
     * STEP 4: Updated to use Spring Security authentication
     */
    @GetMapping("/event/view/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public String viewEvent(@PathVariable Long eventId, Model model) {

        User user = securityHelper.getCurrentUser();

        // Get event details
        Optional<Event> eventOptional = eventService.findById(eventId);

        if (!eventOptional.isPresent()) {
            model.addAttribute("error", "Event not found.");
            model.addAttribute("user", user);
            return "error/404";
        }

        Event event = eventOptional.get();

        // Check access permissions based on user role and event status
        String userRole = user.getRoleName();
        boolean canViewEvent = false;

        if ("chief".equals(userRole) || "admin".equals(userRole)) {
            // Chiefs and admins can view any event
            canViewEvent = true;
        } else if (event.isOngoing()) {
            // Other users can only view ongoing events (active or equipment return)
            canViewEvent = true;
        }

        if (!canViewEvent) {
            model.addAttribute("error", "You don't have permission to view this event.");
            model.addAttribute("user", user);
            return "error/404";
        }

        // Get event responsibilities with their managers
        List<Responsibility> eventResponsibilities = eventService.getEventResponsibilities(eventId);
        Map<Responsibility, List<User>> responsibilitiesWithManagers = new HashMap<>();

        for (Responsibility responsibility : eventResponsibilities) {
            List<User> managers = userService.getResponsibilityManagers(responsibility.getResponsibilityId());
            responsibilitiesWithManagers.put(responsibility, managers);
        }

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("event", event);
        model.addAttribute("userRole", userRole);
        model.addAttribute("eventResponsibilities", responsibilitiesWithManagers);

        return "event-view";
    }

    // ==========================================
    // EVENT-RESPONSIBILITY MANAGEMENT (CHIEF ONLY)
    // ==========================================

    /**
     * Get unassigned responsibilities for event assignment (AJAX endpoint)
     */
    @GetMapping("/chief/events/{eventId}/available-responsibilities")
    @ResponseBody
    @PreAuthorize("hasRole('CHIEF')")
    public List<Map<String, Object>> getAvailableResponsibilities(@PathVariable Long eventId) {

        // Get responsibilities not assigned to THIS specific event
        List<Responsibility> unassignedResponsibilities = eventService.getUnassignedResponsibilities(eventId);

        // Convert to simplified format for frontend
        List<Map<String, Object>> responsibilityList = new ArrayList<>();
        for (Responsibility responsibility : unassignedResponsibilities) {
            Map<String, Object> respMap = new HashMap<>();
            respMap.put("id", responsibility.getResponsibilityId());
            respMap.put("name", responsibility.getResponsibilityName());
            respMap.put("description", responsibility.getDescription());
            responsibilityList.add(respMap);
        }

        return responsibilityList;
    }

    /**
     * Add responsibility to event (Chief only)
     */
    @PostMapping("/chief/events/{eventId}/add-responsibility")
    @ResponseBody
    @PreAuthorize("hasRole('CHIEF')")
    public Map<String, Object> addResponsibilityToEvent(@PathVariable Long eventId,
                                                        @RequestParam Long responsibilityId) {
        Map<String, Object> response = new HashMap<>();

        // Add responsibility to event
        String result = eventService.addResponsibilityToEvent(eventId, responsibilityId);

        if ("success".equals(result)) {
            response.put("success", true);
            response.put("message", "Responsibility added successfully");

            // Get updated responsibilities list for the event
            List<Responsibility> eventResponsibilities = eventService.getEventResponsibilities(eventId);
            List<Map<String, Object>> responsibilitiesData = new ArrayList<>();

            for (Responsibility responsibility : eventResponsibilities) {
                Map<String, Object> respData = new HashMap<>();
                respData.put("id", responsibility.getResponsibilityId());
                respData.put("name", responsibility.getResponsibilityName());
                respData.put("description", responsibility.getDescription());

                // Get managers for this responsibility
                List<User> managers = userService.getResponsibilityManagers(responsibility.getResponsibilityId());
                List<String> managerNames = new ArrayList<>();
                for (User manager : managers) {
                    managerNames.add(manager.getFirstName() + " " + manager.getLastName());
                }
                respData.put("managers", managerNames);

                responsibilitiesData.add(respData);
            }

            response.put("responsibilities", responsibilitiesData);
        } else {
            response.put("success", false);
            response.put("message", result);
        }

        return response;
    }

    /**
     * Remove responsibility from event (Chief only)
     */
    @PostMapping("/chief/events/{eventId}/remove-responsibility")
    @ResponseBody
    @PreAuthorize("hasRole('CHIEF')")
    public Map<String, Object> removeResponsibilityFromEvent(@PathVariable Long eventId,
                                                             @RequestParam Long responsibilityId) {
        Map<String, Object> response = new HashMap<>();

        // Remove responsibility from event
        String result = eventService.removeResponsibilityFromEvent(eventId, responsibilityId);

        if ("success".equals(result)) {
            response.put("success", true);
            response.put("message", "Responsibility removed successfully");

            // Get updated responsibilities list for the event
            List<Responsibility> eventResponsibilities = eventService.getEventResponsibilities(eventId);
            List<Map<String, Object>> responsibilitiesData = new ArrayList<>();

            for (Responsibility responsibility : eventResponsibilities) {
                Map<String, Object> respData = new HashMap<>();
                respData.put("id", responsibility.getResponsibilityId());
                respData.put("name", responsibility.getResponsibilityName());
                respData.put("description", responsibility.getDescription());

                // Get managers for this responsibility
                List<User> managers = userService.getResponsibilityManagers(responsibility.getResponsibilityId());
                List<String> managerNames = new ArrayList<>();
                for (User manager : managers) {
                    managerNames.add(manager.getFirstName() + " " + manager.getLastName());
                }
                respData.put("managers", managerNames);

                responsibilitiesData.add(respData);
            }

            response.put("responsibilities", responsibilitiesData);
        } else {
            response.put("success", false);
            response.put("message", result);
        }

        return response;
    }
}