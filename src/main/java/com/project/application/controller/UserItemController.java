package com.project.application.controller;

import com.project.application.controller.helper.SecurityHelper;
import com.project.application.entity.Item;
import com.project.application.entity.User;
import com.project.application.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * STEP 4: Updated to use Spring Security instead of manual session management
 */
@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()") // STEP 4: Require authentication for all methods
public class UserItemController {

    private final ItemService itemService;
    private final RequestService requestService;
    private final EventService eventService;
    private final SecurityHelper securityHelper;

    /**
     * Handle user item request via AJAX
     * STEP 4: Updated to use Spring Security authentication
     */
    @PostMapping("/user/request-item")
    @ResponseBody
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'CHIEF')") // Only these roles can request items
    public String requestItem(@RequestParam Long itemId) {

        User user = securityHelper.getCurrentUser();

        // Create the request
        String result = requestService.createRequest(user.getUserId(), itemId, "request");

        if ("success".equals(result)) {
            return "success:Item request submitted successfully";
        } else {
            return "error:" + result;
        }
    }

    /**
     * Handle user item return request via AJAX
     * STEP 4: Updated to use Spring Security authentication
     */
    @PostMapping("/user/return-item")
    @ResponseBody
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'CHIEF')") // Only these roles can return items
    public String returnItem(@RequestParam Long itemId) {

        User user = securityHelper.getCurrentUser();

        // Create the return request
        String result = requestService.createRequest(user.getUserId(), itemId, "return");

        if ("success".equals(result)) {
            return "success:Item return request submitted successfully";
        } else {
            return "error:" + result;
        }
    }

    /**
     * Display user's owned items page with event status integration
     * STEP 4: Updated to use Spring Security authentication
     */
    @GetMapping("/user/my-items")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'CHIEF')") // Only these roles can view their items
    public String myItems(Model model) {
        User user = securityHelper.getCurrentUser();

        // Get all items owned by this user
        List<Item> userItems = itemService.getItemsByUserId(user.getUserId());

        // Check if any events allow returns
        boolean canReturnItems = eventService.areItemReturnsAllowed();

        // Check which items the current user has pending return requests for
        List<Long> userPendingReturnItemIds = requestService.getRequestsByUserId(user.getUserId())
                .stream()
                .filter(request -> "return".equals(request.getRequestType()))
                .map(request -> request.getItem().getItemId())
                .toList();

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("userItems", userItems);
        model.addAttribute("itemCount", userItems.size());
        model.addAttribute("canReturnItems", canReturnItems);
        model.addAttribute("activeNavButton", "myitems");
        model.addAttribute("userPendingReturnItemIds", userPendingReturnItemIds);

        return "user-items";
    }
}