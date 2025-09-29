package com.project.application.controller;

import com.project.application.controller.helper.SecurityHelper;
import com.project.application.entity.Role;
import com.project.application.entity.User;
import com.project.application.service.RoleService;
import com.project.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller handling chief-specific operations
 */
@Controller
@RequestMapping("/chief")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CHIEF')")
public class ChiefController {

    private final UserService userService;
    private final RoleService roleService;
    private final SecurityHelper securityHelper;

    /**
     * Display chief user management page
     */
    @GetMapping("/user-list")
    public String chiefUserList(Model model) {
        User user = securityHelper.getCurrentUser();

        // Get managers and users with their responsibilities populated
        List<User> users = userService.getManagersAndUsersWithResponsibilities();

        // Get only manager and user roles for the dropdown
        List<Role> roles = roleService.getAllRoles().stream()
                .filter(role -> "manager".equals(role.getName()) || "user".equals(role.getName()))
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        model.addAttribute("activeNavButton", "userlist");

        return "chief-user-list";
    }

    /**
     * Assign responsibility to user (promotes user to manager)
     */
    @PostMapping("/assign-responsibility")
    public String assignResponsibility(@RequestParam Long userId,
                                       @RequestParam String responsibilityName) {

        // Assign responsibility using service
        String result = userService.assignResponsibility(userId, responsibilityName);

        if ("success".equals(result)) {
            return "redirect:/chief/user-list?success=" +
                    URLEncoder.encode("Responsibility assigned successfully!", StandardCharsets.UTF_8);
        } else {
            return "redirect:/chief/user-list?error=" +
                    URLEncoder.encode(result, StandardCharsets.UTF_8);
        }
    }

    /**
     * Remove responsibility from user
     */
    @PostMapping("/remove-responsibility")
    public String removeResponsibility(@RequestParam Long userId) {

        // Remove responsibility using service
        String result = userService.removeUserFromResponsibility(userId);

        if ("success".equals(result)) {
            return "redirect:/chief/user-list?success=" +
                    URLEncoder.encode("Responsibility removed successfully!", StandardCharsets.UTF_8);
        } else {
            return "redirect:/chief/user-list?error=" +
                    URLEncoder.encode(result, StandardCharsets.UTF_8);
        }
    }
}