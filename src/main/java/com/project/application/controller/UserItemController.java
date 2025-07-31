package com.project.application.controller;

import com.project.application.entity.Item;
import com.project.application.entity.User;
import com.project.application.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserItemController {

    private final ItemService itemService;
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
     * Handle user item request via AJAX
     */
    @PostMapping("/user/request-item")
    @ResponseBody
    public String requestItem(@RequestParam Long itemId,
                              HttpSession session) {

        // Check if user is logged in
        if (!isUserLoggedIn(session)) {
            return "error:Please log in to request items";
        }

        User user = getLoggedInUser(session);

        // Users, managers, and chiefs can request items, but not admins
        if ("admin".equals(user.getRoleName())) {
            return "error:Your role cannot request items";
        }

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
     */
    @PostMapping("/user/return-item")
    @ResponseBody
    public String returnItem(@RequestParam Long itemId,
                             HttpSession session) {

        // Check if user is logged in
        if (!isUserLoggedIn(session)) {
            return "error:Please log in to return items";
        }

        User user = getLoggedInUser(session);

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
     */
    @GetMapping("/user/my-items")
    public String myItems(HttpSession session, Model model) {
        // Check if user is logged in
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);

        // Users, managers, and chiefs can view their items, but not admins
        if ("admin".equals(user.getRoleName())) {
            return "error/404";
        }

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