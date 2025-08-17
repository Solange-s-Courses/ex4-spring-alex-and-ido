package com.project.application.controller;

import com.project.application.entity.User;
import com.project.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling authentication and user profile management
 * Updated to work with Spring Security and fixed form binding issues
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // ==========================================
    // AUTHENTICATION ROUTES
    // ==========================================

    /**
     * Home page redirect
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    /**
     * Show registration form
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // No need to add user object for @RequestParam approach
        return "register";
    }

    /**
     * Process user registration
     * FIXED: Using @RequestParam instead of @ModelAttribute to avoid binding issues
     */
    @PostMapping("/register")
    public String registerUser(@RequestParam String emailAddress,
                               @RequestParam String phoneNumber,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String password,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Validate required fields
        if (emailAddress == null || emailAddress.trim().isEmpty() ||
                phoneNumber == null || phoneNumber.trim().isEmpty() ||
                firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {

            model.addAttribute("error", "All fields are required");
            return "register";
        }

        // Create User object from form parameters
        User user = new User();
        user.setEmailAddress(emailAddress.trim());
        user.setPhoneNumber(phoneNumber.trim());
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setPassword(password);

        // Use service to register user (includes role assignment and validation)
        String result = userService.registerUser(user);

        if ("success".equals(result)) {
            // Use URL parameter for success message to ensure it displays on redirect
            return "redirect:/login?registered=true";
        } else {
            model.addAttribute("error", result);
            return "register";
        }
    }

    /**
     * Show login form
     * Spring Security handles authentication
     */
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    /**
     * Helper method to get current authenticated user
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.findByEmailAddress(auth.getName()).orElse(null);
        }
        return null;
    }

    // ==========================================
    // USER PROFILE MANAGEMENT
    // ==========================================

    /**
     * Show user profile information
     */
    @GetMapping("/user-info")
    public String userInfo(Model model) {
        User loggedInUser = getCurrentUser();
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("activeNavButton", "userinfo");
        return "user-info";
    }

    /**
     * Change username
     */
    @PostMapping("/change-name")
    public String changeName(@RequestParam String firstName,
                             @RequestParam String lastName,
                             RedirectAttributes redirectAttributes) throws java.io.UnsupportedEncodingException {

        User loggedInUser = getCurrentUser();
        if (loggedInUser == null) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Session expired! Please log in again.", "UTF-8");
        }

        String result = userService.updateUserName(loggedInUser, firstName, lastName);

        if ("success".equals(result)) {
            return "redirect:/user-info?success=" + java.net.URLEncoder.encode("Name changed successfully!", "UTF-8");
        } else {
            return "redirect:/user-info?error=" + java.net.URLEncoder.encode(result, "UTF-8");
        }
    }

    /**
     * Change user phone number
     */
    @PostMapping("/change-phone")
    public String changePhone(@RequestParam String phoneNumber,
                              RedirectAttributes redirectAttributes) throws java.io.UnsupportedEncodingException {

        User loggedInUser = getCurrentUser();
        if (loggedInUser == null) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Session expired! Please log in again.", "UTF-8");
        }

        String result = userService.updateUserPhone(loggedInUser, phoneNumber);

        if ("success".equals(result)) {
            return "redirect:/user-info?success=" + java.net.URLEncoder.encode("Phone number changed successfully!", "UTF-8");
        } else {
            return "redirect:/user-info?error=" + java.net.URLEncoder.encode(result, "UTF-8");
        }
    }
}