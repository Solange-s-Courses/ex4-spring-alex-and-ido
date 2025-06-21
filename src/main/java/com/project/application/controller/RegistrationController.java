package com.project.application.controller;

import com.project.application.entity.User;
import com.project.application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

// let spring know this is a web controller that handles HTTP requests
@Controller
public class RegistrationController {

    // Dependency Injection
    @Autowired
    private UserRepository userRepository;  // Interface that handles database operations for User entities

    // Handles GET requests to /register URL
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
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
}