package com.project.application.controller;

import com.project.application.entity.Role;
import com.project.application.entity.User;
import com.project.application.service.RoleService;
import com.project.application.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    private static final String LOGGED_IN_USER = "loggedInUser";

    // helper methods
    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute(LOGGED_IN_USER);
    }
    private boolean isUserLoggedIn(HttpSession session) {
        return getLoggedInUser(session) != null;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpSession session) {
        if (isUserLoggedIn(session)) {
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

    @GetMapping("/login")
    public String loginForm(HttpSession session) {
        if (isUserLoggedIn(session)) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        User user = userService.loginUser(email, password);

        if (user != null) {
            // Store user information in session (including role info)
            session.setAttribute(LOGGED_IN_USER, user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userEmail", user.getEmailAddress());
            session.setAttribute("userFirstName", user.getFirstName());
            session.setAttribute("userLastName", user.getLastName());
            session.setAttribute("userPhone", user.getPhoneNumber());

            // NEW: Store role information in session
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userRoleName", user.getRoleName());

            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", loggedInUser);
        model.addAttribute("userRole", session.getAttribute("userRoleName"));
        return "dashboard";
    }

    @GetMapping("/user-info")
    public String userInfo(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        model.addAttribute("userRole", session.getAttribute("userRoleName"));
        return "user-info";
    }

    @PostMapping("/change-name")
    public String changeName(@RequestParam String firstName,
                             @RequestParam String lastName,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired! Please log in again.");
            return "redirect:/login";
        }

        String result = userService.updateUserName(loggedInUser, firstName, lastName);

        if ("success".equals(result)) {
            session.setAttribute(LOGGED_IN_USER, loggedInUser);
            session.setAttribute("userFirstName", loggedInUser.getFirstName());
            session.setAttribute("userLastName", loggedInUser.getLastName());

            redirectAttributes.addFlashAttribute("success", "Name changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/user-info";
    }

    @PostMapping("/change-phone")
    public String changePhone(@RequestParam String phoneNumber,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired! Please log in again.");
            return "redirect:/login";
        }

        String result = userService.updateUserPhone(loggedInUser, phoneNumber);

        if ("success".equals(result)) {
            session.setAttribute(LOGGED_IN_USER, loggedInUser);
            session.setAttribute("userPhone", loggedInUser.getPhoneNumber());

            redirectAttributes.addFlashAttribute("success", "Phone number changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/user-info";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/admin")
    public String adminPage(HttpSession session, Model model) {
        // Check if user is logged in
        User user = (User) session.getAttribute(LOGGED_IN_USER);
        if (user == null) {
            return "redirect:/login";
        }

        // Check if user has admin role
        if (!"admin".equals(user.getRole().getName())) {
            return "error/404"; // Redirect to 404 for non-admin users
        }

        // Get all non-admin users for the user list
        List<User> users = userService.getAllNonAdminUsers();

        // Get all roles for the dropdown (excluding admin)
        List<Role> roles = roleService.getAllRoles().stream()
                .filter(role -> !"admin".equals(role.getName()))
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        return "admin";
    }

    @PostMapping("/admin/delete-user")
    public String deleteUser(@RequestParam Long userId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        // Check if user is logged in and is admin
        User user = (User) session.getAttribute(LOGGED_IN_USER);
        if (user == null) {
            return "redirect:/login";
        }

        if (!"admin".equals(user.getRole().getName())) {
            return "error/404";
        }

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

    @PostMapping("/admin/edit-user")
    public String editUser(@RequestParam Long userId,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String roleName,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        // Check if user is logged in and is admin
        User user = (User) session.getAttribute(LOGGED_IN_USER);
        if (user == null) {
            return "redirect:/login";
        }

        if (!"admin".equals(user.getRole().getName())) {
            return "error/404";
        }

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
}