package com.project.application.controller;

import com.project.application.entity.Item;
import com.project.application.entity.User;
import com.project.application.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ResponsibilityViewController {

    private final ItemService itemService;
    private final UserService userService;
    private final ResponsibilityService responsibilityService;
    private final RequestService requestService;
    private final EventService eventService;

    private static final String LOGGED_IN_USER = "loggedInUser";

    // Helper method to get logged in user
    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute(LOGGED_IN_USER);
    }

    // Helper method to check if user is logged in
    private boolean isUserLoggedIn(HttpSession session) {
        return getLoggedInUser(session) != null;
    }

    /**
     * Display responsibility details with item list for all users
     * ENHANCED: Now includes request functionality, event status, and available items count
     */
    @GetMapping("/responsibility/view/{id}")
    public String viewResponsibility(@PathVariable Long id,
                                     HttpSession session,
                                     Model model) {
        // Check if user is logged in
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);

        // Get responsibility details
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findById(id);

        if (!responsibilityOptional.isPresent()) {
            model.addAttribute("error", "Responsibility not found.");
            model.addAttribute("user", user);
            return "error/404";
        }

        com.project.application.entity.Responsibility responsibility = responsibilityOptional.get();

        // Get all items for this responsibility
        List<Item> items = itemService.getItemsByResponsibilityId(id);

        // Get managers for this responsibility
        List<com.project.application.entity.User> managers = userService.getResponsibilityManagers(id);

        // Add request-related data for users
        // Check which items the current user has already requested
        List<Long> userRequestedItemIds = requestService.getRequestsByUserId(user.getUserId())
                .stream()
                .map(request -> request.getItem().getItemId())
                .toList();

        // Add event status data
        boolean canRequestItems = eventService.isResponsibilityInActiveEvent(id);
        boolean canReturnItems = eventService.isResponsibilityInReturnEvent(id);

        // Calculate available items count and total items count
        long availableItemsCount = items.stream()
                .filter(item -> "Available".equals(item.getStatus()))
                .count();
        int totalItemsCount = items.size();

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("responsibility", responsibility);
        model.addAttribute("items", items);
        model.addAttribute("responsibilityManagers", managers);
        model.addAttribute("userRequestedItemIds", userRequestedItemIds);
        model.addAttribute("canRequestItems", canRequestItems);
        model.addAttribute("canReturnItems", canReturnItems);
        model.addAttribute("availableItemsCount", availableItemsCount);
        model.addAttribute("totalItemsCount", totalItemsCount);

        return "responsibility-view";
    }
}