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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller handling admin-specific operations
 * STEP 4: Updated to use Spring Security with @PreAuthorize annotations
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // STEP 4: Secure entire controller for admin role
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;
    private final SecurityHelper securityHelper;

    // ==========================================
    // ADMIN USER MANAGEMENT
    // ==========================================

    /**
     * Display admin dashboard with user management
     */
    @GetMapping
    public String adminPage(Model model) {
        User user = securityHelper.getCurrentUser();

        // Get all non-admin users for the user list
        List<User> users = userService.getAllNonAdminUsers();

        // Get all roles for the dropdown (excluding admin)
        List<Role> roles = roleService.getAllRoles().stream()
                .filter(role -> !"admin".equals(role.getName()))
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        model.addAttribute("activeNavButton", "admin");

        return "admin";
    }

    /**
     * Delete individual user
     */
    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam Long userId,
                             RedirectAttributes redirectAttributes) {

        User user = securityHelper.getCurrentUser();

        // Prevent admin from deleting themselves
        if (user.getUserId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account!");
            return "redirect:/admin";
        }

        String result = userService.deleteUser(userId);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/admin";
    }

    /**
     * Edit user information and role
     */
    @PostMapping("/edit-user")
    public String editUser(@RequestParam Long userId,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String roleName,
                           RedirectAttributes redirectAttributes) {

        User user = securityHelper.getCurrentUser();

        // Prevent admin from editing themselves
        if (user.getUserId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot edit your own account!");
            return "redirect:/admin";
        }

        String result = userService.updateUserByAdmin(userId, firstName, lastName, roleName);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/admin";
    }

    // ==========================================
    // BULK OPERATIONS
    // ==========================================

    /**
     * Delete all non-admin users (requires password confirmation)
     */
    @PostMapping("/delete-all-users")
    public String deleteAllUsers(@RequestParam String adminPassword,
                                 RedirectAttributes redirectAttributes) {

        User user = securityHelper.getCurrentUser();

        // Verify admin password
        if (!userService.verifyAdminPassword(user.getUserId(), adminPassword)) {
            redirectAttributes.addFlashAttribute("error", "Invalid password! Operation cancelled.");
            return "redirect:/admin";
        }

        String result = userService.deleteAllNonAdminUsers();

        if (result.startsWith("success:")) {
            String countStr = result.substring(8); // Remove "success:" prefix
            redirectAttributes.addFlashAttribute("success",
                    "Successfully deleted " + countStr + " users from the database!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/admin";
    }

    // ==========================================
    // AJAX ENDPOINTS
    // ==========================================

    /**
     * Verify admin password (AJAX endpoint for UI validation)
     */
    @PostMapping("/verify-password")
    @ResponseBody
    public String verifyPassword(@RequestParam String adminPassword) {

        User user = securityHelper.getCurrentUser();

        // Verify password using service (handles both BCrypt and legacy passwords)
        if (userService.verifyAdminPassword(user.getUserId(), adminPassword)) {
            return "valid";
        } else {
            return "invalid";
        }
    }
}