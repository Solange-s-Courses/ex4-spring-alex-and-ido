package com.project.application.controller;

import com.project.application.controller.helper.AuthenticationHelper;
import com.project.application.controller.helper.AccessControlHelper;
import com.project.application.entity.User;
import com.project.application.service.EventService;
import com.project.application.entity.Event;
import com.project.application.service.ResponsibilityService;
import com.project.application.entity.Responsibility;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

/**
 * Controller handling dashboard display functionality
 * Provides the main dashboard view with events and responsibilities based on user role
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ResponsibilityService responsibilityService;
    private final EventService eventService;
    private final AuthenticationHelper authHelper;
    private final AccessControlHelper accessControl;

    // ==========================================
    // DASHBOARD DISPLAY
    // ==========================================

    /**
     * Display main dashboard with events and responsibilities
     * - Chiefs see all their events (including not-active)
     * - Other users see only ongoing events (active/equipment return)
     * - Shows responsibilities with their assigned managers
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loggedInUser = authHelper.getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Get all responsibilities with their managers for the dashboard
        Map<Responsibility, List<User>> responsibilitiesWithManagers = responsibilityService.getAllResponsibilitiesWithManagers();

        // Get events based on user role
        List<Event> events;
        if ("chief".equals(loggedInUser.getRoleName())) {
            // Chiefs see all events they created
            events = eventService.getAllEvents();
        } else {
            // Other users see only ongoing events
            events = eventService.getOngoingEvents();
        }

        model.addAttribute("user", loggedInUser);
        model.addAttribute("userRole", authHelper.getUserRole(session));
        model.addAttribute("responsibilitiesWithManagers", responsibilitiesWithManagers);
        model.addAttribute("events", events);

        return "dashboard";
    }
}