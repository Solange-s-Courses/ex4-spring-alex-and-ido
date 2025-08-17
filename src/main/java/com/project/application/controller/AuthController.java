package com.project.application.controller;

import com.project.application.entity.User;
import com.project.application.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling authentication and user profile management
 * STEP 3: Updated to work with Spring Security
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
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Process user registration
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Use service to register user (includes role assignment)
        String result = userService.registerUser(user);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Registration successful!");
            return "redirect:/register";
        } else {
            model.addAttribute("error", result);
            return "register";
        }
    }

    /**
     * Show login form
     * STEP 3: Simplified - Spring Security handles authentication
     */
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // STEP 3: Login POST is now handled by Spring Security automatically
    // No need for manual @PostMapping("/login") method

    /**
     * STEP 3: Helper method to get current authenticated user
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
     * STEP 3: Updated to use Spring Security authentication
     * User data now provided globally by ControllerAdvice
     */
    @GetMapping("/user-info")
    public String userInfo(Model model) {
        User loggedInUser = getCurrentUser();
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        // Only add page-specific attributes (user data added globally)
        model.addAttribute("activeNavButton", "userinfo");

        return "user-info";
    }

    /**
     * Change username
     * STEP 3: Updated to use Spring Security authentication
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
     * STEP 3: Updated to use Spring Security authentication
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