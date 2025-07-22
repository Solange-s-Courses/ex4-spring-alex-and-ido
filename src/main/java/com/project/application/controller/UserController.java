package com.project.application.controller;

import com.project.application.entity.Role;
import com.project.application.entity.User;
import com.project.application.service.RoleService;
import com.project.application.service.UserService;
import com.project.application.service.EventService;
import com.project.application.entity.Event;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import com.project.application.service.ResponsibilityService;
import com.project.application.entity.Responsibility;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final ResponsibilityService responsibilityService;
    private final EventService eventService;

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

            // Store role information in session
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userRoleName", user.getRoleName());

            // Store responsibility name in session for managers
            if ("manager".equals(user.getRoleName())) {
                String responsibilityName = userService.getUserResponsibilityName(user.getUserId());
                session.setAttribute("userResponsibilityName", responsibilityName);
            } else {
                session.setAttribute("userResponsibilityName", null);
            }

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

        // Get all responsibilities with their managers for the dashboard
        Map<Responsibility, List<User>> responsibilitiesWithManagers = responsibilityService.getAllResponsibilitiesWithManagers();

        // Get events based on user role
        List<Event> events;
        if ("chief".equals(loggedInUser.getRoleName())) {
            // Chiefs see all events they created
            events = eventService.getAllEvents();
        } else {
            // Other users see only ongoing events
            events = eventService.getOngoingEvents();
        }

        model.addAttribute("user", loggedInUser);
        model.addAttribute("userRole", session.getAttribute("userRoleName"));
        model.addAttribute("responsibilitiesWithManagers", responsibilitiesWithManagers);
        model.addAttribute("events", events);

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

    // Chief Routes
    @GetMapping("/chief/user-list")
    public String chiefUserList(HttpSession session, Model model) {
        // Check if user is logged in
        User user = (User) session.getAttribute(LOGGED_IN_USER);
        if (user == null) {
            return "redirect:/login";
        }

        // Check if user has chief role
        if (!"chief".equals(user.getRole().getName())) {
            return "error/404";
        }

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

    // Add this method for assigning responsibility
    @PostMapping("/chief/assign-responsibility")
    public String assignResponsibility(@RequestParam Long userId,
                                       @RequestParam String responsibilityName,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        // Check if user is logged in and is chief
        User user = (User) session.getAttribute(LOGGED_IN_USER);
        if (user == null) {
            return "redirect:/login";
        }

        if (!"chief".equals(user.getRole().getName())) {
            return "error/404";
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

    // Add this method for removing responsibility
    @PostMapping("/chief/remove-responsibility")
    public String removeResponsibility(@RequestParam Long userId,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        // Check if user is logged in and is chief
        User user = (User) session.getAttribute(LOGGED_IN_USER);
        if (user == null) {
            return "redirect:/login";
        }

        if (!"chief".equals(user.getRole().getName())) {
            return "error/404";
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

    /**
     * Create new event (Chief only)
     */
    @PostMapping("/chief/events/create")
    public String createEvent(@RequestParam String eventName,
                              @RequestParam(required = false) String description,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        // Check if user is logged in and is chief
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        if (!"chief".equals(user.getRoleName())) {
            return "error/404";
        }

        // Create event using service
        String result = eventService.createEvent(eventName, description);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Event created successfully!");
            return "redirect:/dashboard";
        } else {
            // Error occurred - reload dashboard with form data and error message

            // Get all responsibilities with their managers for the dashboard
            Map<Responsibility, List<User>> responsibilitiesWithManagers = responsibilityService.getAllResponsibilitiesWithManagers();

            // Get all events for chief
            List<Event> events = eventService.getAllEvents();

            // Add all necessary data to model
            model.addAttribute("user", user);
            model.addAttribute("userRole", user.getRoleName());
            model.addAttribute("responsibilitiesWithManagers", responsibilitiesWithManagers);
            model.addAttribute("events", events);

            // Add form data to preserve user input
            model.addAttribute("eventFormData", new EventFormData(eventName, description));

            // Add error message for the form
            model.addAttribute("eventFormError", result);

            // Add flag to show the form
            model.addAttribute("showEventForm", true);

            return "dashboard";
        }
    }

    // Helper class for form data
    @Getter
    public static class EventFormData {
        private final String eventName;
        private final String description;

        public EventFormData(String eventName, String description) {
            this.eventName = eventName;
            this.description = description;
        }

    }

    /**
     * Delete event (Chief only, not-active events only)
     */
    @PostMapping("/chief/events/delete")
    public String deleteEvent(@RequestParam Long eventId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        // Check if user is logged in and is chief
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        if (!"chief".equals(user.getRoleName())) {
            return "error/404";
        }

        // Delete event using service
        String result = eventService.deleteEvent(eventId);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Event deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/dashboard";
    }

    /**
     * Display event details page for authorized users
     */
    @GetMapping("/event/view/{eventId}")
    public String viewEvent(@PathVariable Long eventId,
                            HttpSession session,
                            Model model) {

        // Check if user is logged in
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        // Get event details
        Optional<Event> eventOptional = eventService.findById(eventId);

        if (!eventOptional.isPresent()) {
            model.addAttribute("error", "Event not found.");
            model.addAttribute("user", user);
            return "error/404";
        }

        Event event = eventOptional.get();

        // Check access permissions based on user role and event status
        String userRole = user.getRoleName();
        boolean canViewEvent = false;

        if ("chief".equals(userRole) || "admin".equals(userRole)) {
            // Chiefs and admins can view any event
            canViewEvent = true;
        } else if (event.isOngoing()) {
            // Other users can only view ongoing events (active or equipment return)
            canViewEvent = true;
        }

        if (!canViewEvent) {
            model.addAttribute("error", "You don't have permission to view this event.");
            model.addAttribute("user", user);
            return "error/404";
        }

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("event", event);
        model.addAttribute("userRole", userRole);

        return "event-view";
    }

    // Admin Routes
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

    @PostMapping("/admin/delete-all-users")
    public String deleteAllUsers(@RequestParam String adminPassword,
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

    @PostMapping("/admin/verify-password")
    @ResponseBody
    public String verifyPassword(@RequestParam String adminPassword,
                                 HttpSession session) {

        User user = (User) session.getAttribute(LOGGED_IN_USER);
        if (user == null || !"admin".equals(user.getRole().getName())) {
            return "invalid";
        }

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