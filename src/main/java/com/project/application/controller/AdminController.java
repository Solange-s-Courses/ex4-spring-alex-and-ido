package com.project.application.controller;

import com.project.application.controller.helper.SecurityHelper;
import com.project.application.entity.Item;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @GetMapping("/user-management")
    @ResponseBody
    public Map<String, Object> getUserManagementData() {
        try {
            List<User> users = userService.getAllUsersForManagement();

            List<Map<String, Object>> userList = users.stream()
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", user.getUserId());
                        userMap.put("fullName", capitalizeNames(user.getFirstName(), user.getLastName()));
                        userMap.put("email", user.getEmailAddress()); // Add email field
                        userMap.put("phone", user.getPhoneNumber());
                        userMap.put("role", capitalizeFirst(user.getRoleName()));
                        userMap.put("isChief", "chief".equals(user.getRoleName()));
                        return userMap;
                    })
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("users", userList);
            response.put("totalUsers", users.size());
            return response;

        } catch (Exception e) {
            return Map.of("users", List.of(), "totalUsers", 0, "error", "Failed to load users");
        }
    }

    @PostMapping("/promote-chief")
    @ResponseBody
    public Map<String, Object> promoteToChief(@RequestParam Long userId) {
        try {
            String result = userService.promoteToChief(userId);

            Map<String, Object> response = new HashMap<>();
            if ("success".equals(result)) {
                response.put("success", true);
                response.put("message", "User promoted to Chief successfully");
            } else {
                response.put("success", false);
                response.put("message", result);
            }
            return response;

        } catch (Exception e) {
            return Map.of("success", false, "message", "Failed to promote user: " + e.getMessage());
        }
    }

    @PostMapping("/demote-chief")
    @ResponseBody
    public Map<String, Object> demoteChief(@RequestParam Long userId) {
        try {
            // Check if this is the last chief
            boolean isLastChief = userService.isLastChief(userId);

            String result = userService.demoteChief(userId);

            Map<String, Object> response = new HashMap<>();
            if ("success".equals(result)) {
                response.put("success", true);
                response.put("message", "Chief demoted successfully");
                response.put("wasLastChief", isLastChief);
            } else {
                response.put("success", false);
                response.put("message", result);
                response.put("wasLastChief", false);
            }
            return response;

        } catch (Exception e) {
            return Map.of("success", false, "message", "Failed to demote chief: " + e.getMessage(), "wasLastChief", false);
        }
    }

    @GetMapping("/check-last-chief")
    @ResponseBody
    public Map<String, Object> checkLastChief(@RequestParam Long userId) {
        try {
            boolean isLastChief = userService.isLastChief(userId);
            return Map.of("isLastChief", isLastChief);
        } catch (Exception e) {
            return Map.of("isLastChief", false, "error", "Failed to check chief status");
        }
    }

    // Helper method to capitalize names for display
    private String capitalizeNames(String firstName, String lastName) {
        return capitalizeFirst(firstName) + " " + capitalizeFirst(lastName);
    }

    // Helper method to capitalize first letter
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @PostMapping("/delete-user")
    @ResponseBody
    public Map<String, Object> deleteUser(@RequestParam Long userId) {
        try {
            // First check if user exists and get their details for validation
            Optional<User> userOptional = userService.findById(userId);
            if (userOptional.isEmpty()) {
                return Map.of("success", false, "message", "User not found");
            }

            User userToDelete = userOptional.get();

            // Prevent deleting admin users
            if ("admin".equals(userToDelete.getRoleName())) {
                return Map.of("success", false, "message", "Cannot delete admin users");
            }

            // Get username for success message
            String userName = capitalizeNames(userToDelete.getFirstName(), userToDelete.getLastName());

            // Call the delete service method
            String result = userService.deleteUser(userId);

            Map<String, Object> response = new HashMap<>();
            if ("success".equals(result)) {
                response.put("success", true);
                response.put("message", "User '" + userName + "' deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", result);
            }
            return response;

        } catch (Exception e) {
            return Map.of("success", false, "message", "Failed to delete user: " + e.getMessage());
        }
    }

    @GetMapping("/managers-info")
    @ResponseBody
    public Map<String, Object> getManagersInfo() {
        try {
            List<User> managers = userService.getAllNonAdminUsers().stream()
                    .filter(u -> "manager".equals(u.getRoleName()))
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("managerCount", managers.size());
            response.put("managers", managers.stream().map(u ->
                    capitalizeNames(u.getFirstName(), u.getLastName())).collect(java.util.stream.Collectors.toList()));

            return response;
        } catch (Exception e) {
            return Map.of("managerCount", 0, "managers", List.of(), "error", "Failed to load manager info");
        }
    }

    @PostMapping("/demote-all-managers")
    @ResponseBody
    public Map<String, Object> demoteAllManagers() {
        try {
            String result = userService.demoteAllManagers();

            if (result.startsWith("success:")) {
                int count = Integer.parseInt(result.substring(8));
                return Map.of("success", true, "message", count + " managers demoted successfully", "count", count);
            } else {
                return Map.of("success", false, "message", result);
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "Failed to demote managers: " + e.getMessage());
        }
    }

    @GetMapping("/chiefs-info")
    @ResponseBody
    public Map<String, Object> getChiefsInfo() {
        try {
            List<User> chiefs = userService.getAllNonAdminUsers().stream()
                    .filter(u -> "chief".equals(u.getRoleName()))
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("chiefCount", chiefs.size());
            response.put("chiefs", chiefs.stream().map(u ->
                    capitalizeNames(u.getFirstName(), u.getLastName())).collect(java.util.stream.Collectors.toList()));

            return response;
        } catch (Exception e) {
            return Map.of("chiefCount", 0, "chiefs", List.of(), "error", "Failed to load chief info");
        }
    }

    @PostMapping("/demote-all-chiefs")
    @ResponseBody
    public Map<String, Object> demoteAllChiefs() {
        try {
            String result = userService.demoteAllChiefs();

            if (result.startsWith("success:")) {
                int count = Integer.parseInt(result.substring(8));
                return Map.of("success", true, "message", count + " chiefs demoted successfully", "count", count);
            } else {
                return Map.of("success", false, "message", result);
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "Failed to demote chiefs: " + e.getMessage());
        }
    }

    @GetMapping("/all-users-info")
    @ResponseBody
    public Map<String, Object> getAllUsersInfo() {
        try {
            List<User> allUsers = userService.getAllNonAdminUsers();

            Map<String, Object> response = new HashMap<>();
            response.put("userCount", allUsers.size());
            response.put("users", allUsers.stream().map(u ->
                    capitalizeNames(u.getFirstName(), u.getLastName())).collect(java.util.stream.Collectors.toList()));

            return response;
        } catch (Exception e) {
            return Map.of("userCount", 0, "users", List.of(), "error", "Failed to load user info");
        }
    }

    @PostMapping("/delete-all-users")
    @ResponseBody
    public Map<String, Object> deleteAllUsers() {
        try {
            String result = userService.deleteAllNonAdminUsers();

            if (result.startsWith("success:")) {
                int count = Integer.parseInt(result.substring(8));
                return Map.of("success", true, "message", count + " users deleted successfully", "count", count);
            } else {
                return Map.of("success", false, "message", result);
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "Failed to delete users: " + e.getMessage());
        }
    }

    // ========== ITEM BULK OPERATIONS ==========

    @GetMapping("/items-info")
    @ResponseBody
    public Map<String, Object> getItemsInfo() {
        try {
            List<Item> allItems = itemService.getAllItems();

            // Count items by status
            long inUseCount = allItems.stream()
                    .filter(item -> "In Use".equals(item.getStatus()) && item.getUser() != null)
                    .count();

            Map<String, Object> response = new HashMap<>();
            response.put("totalItemCount", allItems.size());
            response.put("inUseCount", inUseCount);

            return response;
        } catch (Exception e) {
            return Map.of("totalItemCount", 0, "inUseCount", 0, "error", "Failed to load item info");
        }
    }

    @PostMapping("/return-all-inuse-items")
    @ResponseBody
    public Map<String, Object> returnAllInUseItems() {
        try {
            String result = itemService.returnAllInUseItems();

            if (result.startsWith("success:")) {
                int count = Integer.parseInt(result.substring(8));
                return Map.of("success", true, "message", count + " in-use items returned successfully", "count", count);
            } else {
                return Map.of("success", false, "message", result);
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "Failed to return items: " + e.getMessage());
        }
    }

    @PostMapping("/make-all-items-unavailable")
    @ResponseBody
    public Map<String, Object> makeAllItemsUnavailable() {
        try {
            String result = itemService.makeAllItemsUnavailable();

            if (result.startsWith("success:")) {
                int count = Integer.parseInt(result.substring(8));
                return Map.of("success", true, "message", count + " items marked as unavailable", "count", count);
            } else {
                return Map.of("success", false, "message", result);
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "Failed to update items: " + e.getMessage());
        }
    }

    @PostMapping("/delete-all-items")
    @ResponseBody
    public Map<String, Object> deleteAllItems() {
        try {
            String result = itemService.deleteAllItems();

            if (result.startsWith("success:")) {
                int count = Integer.parseInt(result.substring(8));
                return Map.of("success", true, "message", count + " items deleted successfully", "count", count);
            } else {
                return Map.of("success", false, "message", result);
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", "Failed to delete items: " + e.getMessage());
        }
    }
}