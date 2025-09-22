package com.project.application.controller;

import com.project.application.controller.helper.SecurityHelper;
import com.project.application.entity.User;
import com.project.application.service.EventService;
import com.project.application.entity.Event;
import com.project.application.service.ResponsibilityService;
import com.project.application.entity.Responsibility;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

/**
 * Controller handling dashboard display functionality
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ResponsibilityService responsibilityService;
    private final EventService eventService;
    private final SecurityHelper securityHelper;

    /**
     * Display main dashboard with events and responsibilities
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get current authenticated user using Spring Security
        User loggedInUser = securityHelper.getCurrentUser();
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Get all responsibilities with their managers for the dashboard
        Map<Responsibility, List<User>> responsibilitiesWithManagers = responsibilityService.getAllResponsibilitiesWithManagers();

        // Get events based on user role
        List<Event> events;
        if ("chief".equals(loggedInUser.getRoleName()) || "admin".equals(loggedInUser.getRoleName())) {
            // Chiefs and Admins see all events (chiefs can manage, admins view-only)
            events = eventService.getAllEvents();
        } else {
            // Other users see only ongoing events
            events = eventService.getOngoingEvents();
        }

        // Add only dashboard-specific data (user data added globally)
        model.addAttribute("responsibilitiesWithManagers", responsibilitiesWithManagers);
        model.addAttribute("events", events);
        model.addAttribute("activeNavButton", "dashboard");

        return "dashboard";
    }
}