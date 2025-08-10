package com.project.application.controller;

import com.project.application.controller.helper.AuthenticationHelper;
import com.project.application.controller.helper.AccessControlHelper;
import com.project.application.entity.User;
import com.project.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller handling authentication and user profile management
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationHelper authHelper;
    private final AccessControlHelper accessControl;

    // ==========================================
    // AUTHENTICATION ROUTES
    // ==========================================

    /**
     * Home page redirect
     */
    @GetMapping("/")
    public String home(HttpSession session) {
        if (!authHelper.isUserLoggedIn(session)) {
            return "redirect:/login";
        }
        return "redirect:/dashboard";
    }

    /**
     * Show registration form
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpSession session) {
        if (authHelper.isUserLoggedIn(session)) {
            return "redirect:/dashboard";
        }
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
     */
    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (authHelper.isUserLoggedIn(session)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    /**
     * Process user login
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        User user = userService.loginUser(email, password);

        if (user != null) {
            // Store user information in session using helper
            authHelper.setUserSession(session, user);

            // Store responsibility information in session for managers
            if ("manager".equals(user.getRoleName())) {
                String responsibilityName = userService.getUserResponsibilityName(user.getUserId());
                Long responsibilityId = userService.getUserResponsibilityId(user.getUserId());
                authHelper.setResponsibilitySession(session, responsibilityName, responsibilityId);
            } else {
                authHelper.clearResponsibilitySession(session);
            }

            return "redirect:/dashboard";
        } else {
            // Preserve the attempted email and password for user convenience
            model.addAttribute("email", email);
            model.addAttribute("password", password);
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    /**
     * Logout user
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        authHelper.clearSession(session);
        return "redirect:/login";
    }

    // ==========================================
    // USER PROFILE MANAGEMENT
    // ==========================================

    /**
     * Show user profile information
     */
    @GetMapping("/user-info")
    public String userInfo(HttpSession session, Model model) {
        User loggedInUser = authHelper.getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        model.addAttribute("userRole", authHelper.getUserRole(session));
        model.addAttribute("activeNavButton", "userinfo");

        return "user-info";
    }

    /**
     * Change username
     */
    @PostMapping("/change-name")
    public String changeName(@RequestParam String firstName,
                             @RequestParam String lastName,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) throws java.io.UnsupportedEncodingException {

        User loggedInUser = authHelper.getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Session expired! Please log in again.", "UTF-8");
        }

        String result = userService.updateUserName(loggedInUser, firstName, lastName);

        if ("success".equals(result)) {
            // Update session with new user data
            authHelper.updateUserSession(session, loggedInUser);
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
                              HttpSession session,
                              RedirectAttributes redirectAttributes) throws java.io.UnsupportedEncodingException {

        User loggedInUser = authHelper.getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/login?error=" + java.net.URLEncoder.encode("Session expired! Please log in again.", "UTF-8");
        }

        String result = userService.updateUserPhone(loggedInUser, phoneNumber);

        if ("success".equals(result)) {
            // Update session with new user data
            authHelper.updateUserSession(session, loggedInUser);
            return "redirect:/user-info?success=" + java.net.URLEncoder.encode("Phone number changed successfully!", "UTF-8");
        } else {
            return "redirect:/user-info?error=" + java.net.URLEncoder.encode(result, "UTF-8");
        }
    }
}