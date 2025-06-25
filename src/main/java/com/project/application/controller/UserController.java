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

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Trim and convert names to lowercase
        if (user.getFirstName() != null) {
            user.setFirstName(user.getFirstName().trim().toLowerCase());
        }
        if (user.getLastName() != null) {
            user.setLastName(user.getLastName().trim().toLowerCase());
        }

        // Trim other fields
        if (user.getEmailAddress() != null) {
            user.setEmailAddress(user.getEmailAddress().trim());
        }
        if (user.getPhoneNumber() != null) {
            user.setPhoneNumber(user.getPhoneNumber().trim());
        }

        // Convert names to lowercase before validation and saving
        if (user.getFirstName() != null) {
            user.setFirstName(user.getFirstName().toLowerCase());
        }
        if (user.getLastName() != null) {
            user.setLastName(user.getLastName().toLowerCase());
        }

        // Encrypt password before saving
        user.setEncryptedPassword(user.getEncryptedPassword()); // encryption will be added...

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            return "register";
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
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        Optional<User> userOptional = userRepository.findByEmailAddress(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (password.equals(user.getEncryptedPassword())) {
                session.setAttribute("loggedInUser", user);
                return "redirect:/dashboard";
            }
        }

        model.addAttribute("error", "Invalid email or password");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        return "dashboard";
    }

    @GetMapping("/user-info")
    public String userInfo(HttpSession session, Model model) {
        // Check if user is logged in
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login"; // Not logged in, redirect to login
        }

        // User is logged in, show their info
        model.addAttribute("user", loggedInUser);
        return "user-info"; // Returns user-info.html template
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}