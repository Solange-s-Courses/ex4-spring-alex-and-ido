package com.project.application.controller;

import com.project.application.controller.helper.AuthenticationHelper;
import com.project.application.controller.helper.AccessControlHelper;
import com.project.application.entity.Role;
import com.project.application.entity.User;
import com.project.application.service.RoleService;
import com.project.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller handling chief-specific operations
 * - User management and role assignments
 * - Responsibility assignments to users (promoting them to managers)
 * - Chief user list management
 */
@Controller
@RequestMapping("/chief")
@RequiredArgsConstructor
public class ChiefController {

    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationHelper authHelper;
    private final AccessControlHelper accessControl;

    // ==========================================
    // CHIEF USER MANAGEMENT
    // ==========================================

    /**
     * Display chief user management page
     */
    @GetMapping("/user-list")
    public String chiefUserList(HttpSession session, Model model) {
        // Check access using helper
        String accessCheck = accessControl.validateAccess(session, "chief");
        if (accessCheck != null) {
            return accessCheck;
        }

        User user = authHelper.getLoggedInUser(session);

        // Get managers and users with their responsibilities populated
        List<User> users = userService.getManagersAndUsersWithResponsibilities();

        // Get only manager and user roles for the dropdown
        List<Role> roles = roleService.getAllRoles().stream()
                .filter(role -> "manager".equals(role.getName()) || "user".equals(role.getName()))
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        return "chief-user-list";
    }

    // ==========================================
    // USER-RESPONSIBILITY MANAGEMENT
    // ==========================================

    /**
     * Assign responsibility to user (promotes user to manager)
     */
    @PostMapping("/assign-responsibility")
    public String assignResponsibility(@RequestParam Long userId,
                                       @RequestParam String responsibilityName,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        // Check access using helper
        String accessCheck = accessControl.validateAccess(session, "chief");
        if (accessCheck != null) {
            return accessCheck;
        }

        // Assign responsibility using service
        String result = userService.assignResponsibility(userId, responsibilityName);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Responsibility assigned successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/chief/user-list";
    }

    /**
     * Remove responsibility from user
     */
    @PostMapping("/remove-responsibility")
    public String removeResponsibility(@RequestParam Long userId,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        // Check access using helper
        String accessCheck = accessControl.validateAccess(session, "chief");
        if (accessCheck != null) {
            return accessCheck;
        }

        // Remove responsibility using service
        String result = userService.removeUserFromResponsibility(userId);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Responsibility removed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/chief/user-list";
    }
}