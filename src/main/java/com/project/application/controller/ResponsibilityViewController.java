package com.project.application.controller;

import com.project.application.controller.helper.SecurityHelper;
import com.project.application.entity.Item;
import com.project.application.entity.User;
import com.project.application.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

/**
 * STEP 4: Updated to use Spring Security instead of manual session management
 */
@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()") // STEP 4: Require authentication for all methods
public class ResponsibilityViewController {

    private final ItemService itemService;
    private final UserService userService;
    private final ResponsibilityService responsibilityService;
    private final RequestService requestService;
    private final EventService eventService;
    private final SecurityHelper securityHelper;

    /**
     * Display responsibility details with item list for all users
     * STEP 4: Updated to use Spring Security authentication
     */
    @GetMapping("/responsibility/view/{id}")
    public String viewResponsibility(@PathVariable Long id, Model model) {
        User user = securityHelper.getCurrentUser();

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
                .filter(request -> "request".equals(request.getRequestType()))
                .map(request -> request.getItem().getItemId())
                .toList();

        // Check which items the current user has pending return requests for
        List<Long> userPendingReturnItemIds = requestService.getRequestsByUserId(user.getUserId())
                .stream()
                .filter(request -> "return".equals(request.getRequestType()))
                .map(request -> request.getItem().getItemId())
                .toList();

        // Add event status data
        boolean canRequestItems = eventService.isResponsibilityInActiveEvent(id);
        boolean canReturnItems = eventService.isResponsibilityInReturnAllowedEvent(id);

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
        model.addAttribute("userPendingReturnItemIds", userPendingReturnItemIds);
        model.addAttribute("canRequestItems", canRequestItems);
        model.addAttribute("canReturnItems", canReturnItems);
        model.addAttribute("availableItemsCount", availableItemsCount);
        model.addAttribute("totalItemsCount", totalItemsCount);

        return "responsibility-view";
    }
}