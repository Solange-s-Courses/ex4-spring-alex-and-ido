package com.project.application.controller;

import com.project.application.controller.helper.SecurityHelper;
import com.project.application.entity.User;
import com.project.application.entity.Event;
import com.project.application.service.UserService;
import com.project.application.service.ItemService;
import com.project.application.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SecurityHelper securityHelper;
    private final UserService userService;
    private final ItemService itemService;
    private final EventService eventService;

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("user", securityHelper.getCurrentUser());
        model.addAttribute("activeNavButton", "admin");
        return "admin";
    }

    @GetMapping("/metrics/user-roles")
    @ResponseBody
    public Map<String, Object> getUserRoleMetrics() {
        return getMetrics(() -> {
            List<User> users = userService.getAllNonAdminUsers();
            Map<String, Integer> counts = new HashMap<>();
            counts.put("chief", 0);
            counts.put("manager", 0);
            counts.put("user", 0);

            users.forEach(user -> {
                String role = user.getRoleName().toLowerCase();
                counts.computeIfPresent(role, (k, v) -> v + 1);
            });

            Map<String, Object> response = new HashMap<>();
            response.put("roleCounts", counts);
            response.put("totalUsers", users.size());
            return response;
        });
    }

    @GetMapping("/metrics/item-status")
    @ResponseBody
    public Map<String, Object> getItemStatusMetrics() {
        return getMetrics(() -> {
            Map<String, Integer> counts = itemService.getItemStatusDistribution();
            int total = counts.values().stream().mapToInt(Integer::intValue).sum();

            Map<String, Object> response = new HashMap<>();
            response.put("statusCounts", counts);
            response.put("totalItems", total);
            return response;
        });
    }

    @GetMapping("/metrics/event-status")
    @ResponseBody
    public Map<String, Object> getEventStatusMetrics() {
        return getMetrics(() -> {
            List<Event> events = eventService.getAllEvents();
            Map<String, Integer> counts = new HashMap<>();
            counts.put("notActive", 0);
            counts.put("active", 0);
            counts.put("equipmentReturn", 0);

            events.forEach(event -> {
                switch (event.getStatus()) {
                    case Event.STATUS_NOT_ACTIVE -> counts.put("notActive", counts.get("notActive") + 1);
                    case Event.STATUS_ACTIVE -> counts.put("active", counts.get("active") + 1);
                    case Event.STATUS_EQUIPMENT_RETURN -> counts.put("equipmentReturn", counts.get("equipmentReturn") + 1);
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("statusCounts", counts);
            response.put("totalEvents", events.size());
            return response;
        });
    }

    private Map<String, Object> getMetrics(MetricsSupplier supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return Map.of("roleCounts", Map.of(), "statusCounts", Map.of(), "totalUsers", 0, "totalItems", 0, "totalEvents", 0);
        }
    }

    @FunctionalInterface
    private interface MetricsSupplier {
        Map<String, Object> get() throws Exception;
    }
}