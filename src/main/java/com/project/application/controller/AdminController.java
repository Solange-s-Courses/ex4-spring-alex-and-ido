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
 * Controller handling admin-specific operations
 * - User management (view, edit, delete)
 * - Bulk operations (delete all users)
 * - Admin password verification
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationHelper authHelper;
    private final AccessControlHelper accessControl;

    // ==========================================
    // ADMIN USER MANAGEMENT
    // ==========================================

    /**
     * Display admin dashboard with user management
     */
    @GetMapping
    public String adminPage(HttpSession session, Model model) {
        // Check access using helper
        String accessCheck = accessControl.validateAccess(session, "admin");
        if (accessCheck != null) {
            return accessCheck;
        }

        User user = authHelper.getLoggedInUser(session);

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
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        // Check access using helper
        String accessCheck = accessControl.validateAccess(session, "admin");
        if (accessCheck != null) {
            return accessCheck;
        }

        User user = authHelper.getLoggedInUser(session);

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
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        // Check access using helper
        String accessCheck = accessControl.validateAccess(session, "admin");
        if (accessCheck != null) {
            return accessCheck;
        }

        User user = authHelper.getLoggedInUser(session);

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
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        // Check access using helper
        String accessCheck = accessControl.validateAccess(session, "admin");
        if (accessCheck != null) {
            return accessCheck;
        }

        User user = authHelper.getLoggedInUser(session);

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
    public String verifyPassword(@RequestParam String adminPassword,
                                 HttpSession session) {

        // Check access using helper
        if (!accessControl.isAdmin(session)) {
            return "invalid";
        }

        User user = authHelper.getLoggedInUser(session);

        // Temporary debug - remove after testing
        System.out.println("Session user password: '" + user.getPassword() + "'");
        System.out.println("Entered password: '" + adminPassword + "'");

        // Simple direct comparison for testing
        if (adminPassword.equals(user.getPassword())) {
            return "valid";
        } else {
            return "invalid";
        }
    }
}