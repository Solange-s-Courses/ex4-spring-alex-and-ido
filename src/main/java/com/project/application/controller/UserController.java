package com.project.application.controller;

import com.project.application.entity.User;
import com.project.application.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.Optional;

// let spring know this is a web controller that handles HTTP requests
@Controller
public class UserController {

    // Dependency Injection
    @Autowired
    private UserRepository userRepository;  // Interface that handles database operations for User entities

    @GetMapping("/")
    public String home(HttpSession session) {
        // Check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login"; // Not logged in, go to login
        }
        return "redirect:/dashboard"; // Logged in, go to dashboard
    }

    // Handles GET requests to /register URL
    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpSession session) {
        // check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return "redirect:/dashboard"; // Logged in, go to dashboard
        }
        // Creates empty User object and sends it to the template
        model.addAttribute("user", new User());
        return "register";  // Returns the name of the Thymeleaf template (register.html)
    }

    // Handles POST requests to /register URL
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        // Encrypt password before saving
        user.setEncryptedPassword(user.getEncryptedPassword()); // encryption will be added...

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "register"; // Return to form with error messages
        }

        // Check if email already exists
        if (userRepository.findByEmailAddress(user.getEmailAddress()).isPresent()) {
            model.addAttribute("error", "Email address already exists!");
            return "register";
        }

        // Check if phone already exists
        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            model.addAttribute("error", "Phone number already exists!");
            return "register";
        }

        try {
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful!");
            return "redirect:/register";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed!");
            return "register";
        }
    }

    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        // Check if user is already logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return "redirect:/dashboard"; // Already logged in, go to dashboard
        }
        return "login"; // Not logged in, show login page
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        // Find user by email - returns Optional<User>
        Optional<User> userOptional = userRepository.findByEmailAddress(email);

        // Check if user exists and password matches
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (password.equals(user.getEncryptedPassword())) {
                // Store user in session
                session.setAttribute("loggedInUser", user);
                return "redirect:/dashboard"; // Redirect to landing page
            }
        }

        // Login failed (user not found OR wrong password)
        model.addAttribute("error", "Invalid email or password");
        return "login"; // Back to login page with error message
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login"; // Not logged in, redirect to login
        }

        // User is logged in, show dashboard
        model.addAttribute("user", loggedInUser);
        return "dashboard"; // Returns dashboard.html template
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear entire session
        return "redirect:/login";
    }
}