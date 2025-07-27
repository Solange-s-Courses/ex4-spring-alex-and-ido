package com.project.application.controller;

import com.project.application.entity.Item;
import com.project.application.entity.User;
import com.project.application.entity.Request;
import com.project.application.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ItemController {

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
     * Display item management page for managers
     */
    @GetMapping("/manager/items")
    public String itemManagement(HttpSession session, Model model) {
        // Check if user is logged in
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);

        // Check if user is manager
        if (!"manager".equals(user.getRoleName())) {
            return "error/404";
        }

        // Get user's responsibility
        String responsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (responsibilityName == null) {
            // Manager has no responsibility assigned
            model.addAttribute("error", "You have no responsibility assigned. Contact your chief to get a responsibility.");
            model.addAttribute("user", user);
            return "manager-no-responsibility";
        }

        // Get responsibility details
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findByName(responsibilityName);

        if (!responsibilityOptional.isPresent()) {
            model.addAttribute("error", "Responsibility not found in system.");
            model.addAttribute("user", user);
            return "error/404";
        }

        com.project.application.entity.Responsibility responsibility = responsibilityOptional.get();

        // Get all items for this responsibility
        List<Item> items = itemService.getItemsByResponsibilityId(responsibility.getResponsibilityId());

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("responsibility", responsibility);
        model.addAttribute("items", items);
        model.addAttribute("itemCount", items.size());

        return "item-management";
    }

    // ======================
    // NEW: MANAGER REQUEST MANAGEMENT
    // ======================

    /**
     * Display manager requests page
     */
    @GetMapping("/manager/requests")
    public String managerRequests(HttpSession session, Model model) {
        // Check if user is logged in
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);

        // Check if user is manager
        if (!"manager".equals(user.getRoleName())) {
            return "error/404";
        }

        // Get user's responsibility
        String responsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (responsibilityName == null) {
            model.addAttribute("error", "You have no responsibility assigned. Contact your chief to get a responsibility.");
            model.addAttribute("user", user);
            return "manager-no-responsibility";
        }

        // Get responsibility details
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findByName(responsibilityName);

        if (!responsibilityOptional.isPresent()) {
            model.addAttribute("error", "Responsibility not found in system.");
            model.addAttribute("user", user);
            return "error/404";
        }

        com.project.application.entity.Responsibility responsibility = responsibilityOptional.get();

        // Get all pending requests for this responsibility
        List<Request> pendingRequests = requestService.getRequestsByResponsibilityId(responsibility.getResponsibilityId());

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("responsibility", responsibility);
        model.addAttribute("requests", pendingRequests);
        model.addAttribute("requestCount", pendingRequests.size());

        return "manager-requests";
    }

    /**
     * Approve a request
     */
    @PostMapping("/manager/approve-request")
    @ResponseBody
    public String approveRequest(@RequestParam Long requestId,
                                 HttpSession session) {

        // Check if user is logged in and is manager
        if (!isUserLoggedIn(session)) {
            return "error:Please log in to approve requests";
        }

        User user = getLoggedInUser(session);
        if (!"manager".equals(user.getRoleName())) {
            return "error:Access denied";
        }

        // Get user's responsibility to verify permission
        String responsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (responsibilityName == null) {
            return "error:You have no responsibility assigned";
        }

        // Get responsibility details
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findByName(responsibilityName);

        if (!responsibilityOptional.isPresent()) {
            return "error:Responsibility not found";
        }

        Long responsibilityId = responsibilityOptional.get().getResponsibilityId();

        // Verify that this request belongs to the manager's responsibility
        Optional<Request> requestOptional = requestService.findById(requestId);
        if (!requestOptional.isPresent()) {
            return "error:Request not found";
        }

        Request request = requestOptional.get();
        if (!request.getResponsibilityId().equals(responsibilityId)) {
            return "error:You don't have permission to manage this request";
        }

        // Approve the request
        String result = requestService.approveRequest(requestId);

        if ("success".equals(result)) {
            return "success:Request approved successfully";
        } else {
            return "error:" + result;
        }
    }

    /**
     * Deny a request
     */
    @PostMapping("/manager/deny-request")
    @ResponseBody
    public String denyRequest(@RequestParam Long requestId,
                              HttpSession session) {

        // Check if user is logged in and is manager
        if (!isUserLoggedIn(session)) {
            return "error:Please log in to deny requests";
        }

        User user = getLoggedInUser(session);
        if (!"manager".equals(user.getRoleName())) {
            return "error:Access denied";
        }

        // Get user's responsibility to verify permission
        String responsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (responsibilityName == null) {
            return "error:You have no responsibility assigned";
        }

        // Get responsibility details
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findByName(responsibilityName);

        if (!responsibilityOptional.isPresent()) {
            return "error:Responsibility not found";
        }

        Long responsibilityId = responsibilityOptional.get().getResponsibilityId();

        // Verify that this request belongs to the manager's responsibility
        Optional<Request> requestOptional = requestService.findById(requestId);
        if (!requestOptional.isPresent()) {
            return "error:Request not found";
        }

        Request request = requestOptional.get();
        if (!request.getResponsibilityId().equals(responsibilityId)) {
            return "error:You don't have permission to manage this request";
        }

        // Deny the request
        String result = requestService.denyRequest(requestId);

        if ("success".equals(result)) {
            return "success:Request denied successfully";
        } else {
            return "error:" + result;
        }
    }

    /**
     * Update responsibility description
     */
    @PostMapping("/manager/items/update-description")
    public String updateDescription(@RequestParam String description,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        // Check if user is logged in and is manager
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);
        if (!"manager".equals(user.getRoleName())) {
            return "error/404";
        }

        // Get user's responsibility
        String responsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (responsibilityName == null) {
            redirectAttributes.addFlashAttribute("error", "You have no responsibility assigned.");
            return "redirect:/manager/items";
        }

        // Get responsibility
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findByName(responsibilityName);

        if (!responsibilityOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Responsibility not found.");
            return "redirect:/manager/items";
        }

        com.project.application.entity.Responsibility responsibility = responsibilityOptional.get();

        // Validate description
        if (description != null) {
            description = description.trim();

            if (description.length() > 200) {
                redirectAttributes.addFlashAttribute("error", "Description cannot exceed 200 characters.");
                return "redirect:/manager/items";
            }

            // If description is empty, set to null
            if (description.isEmpty()) {
                description = null;
            }
        }

        // Update description using service
        String result = responsibilityService.updateDescription(responsibility.getResponsibilityId(), description);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Description updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/manager/items";
    }

    /**
     * Add new item
     */
    @PostMapping("/manager/items/add")
    public String addItem(@RequestParam String itemName,
                          @RequestParam String status,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

        // Check if user is logged in and is manager
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);
        if (!"manager".equals(user.getRoleName())) {
            return "error/404";
        }

        // Get user's responsibility
        String responsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (responsibilityName == null) {
            redirectAttributes.addFlashAttribute("error", "You have no responsibility assigned.");
            return "redirect:/manager/items";
        }

        // Get responsibility ID
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findByName(responsibilityName);

        if (!responsibilityOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Responsibility not found.");
            return "redirect:/manager/items";
        }

        Long responsibilityId = responsibilityOptional.get().getResponsibilityId();

        // Create item
        String result = itemService.createItem(responsibilityId, itemName, status);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Item added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/manager/items";
    }

    /**
     * Update existing item
     */
    @PostMapping("/manager/items/update")
    public String updateItem(@RequestParam Long itemId,
                             @RequestParam String itemName,
                             @RequestParam String status,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        // Check if user is logged in and is manager
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);
        if (!"manager".equals(user.getRoleName())) {
            return "error/404";
        }

        // Get user's responsibility
        String responsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (responsibilityName == null) {
            redirectAttributes.addFlashAttribute("error", "You have no responsibility assigned.");
            return "redirect:/manager/items";
        }

        // Get responsibility ID for permission check
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findByName(responsibilityName);

        if (!responsibilityOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Responsibility not found.");
            return "redirect:/manager/items";
        }

        Long responsibilityId = responsibilityOptional.get().getResponsibilityId();

        // Verify user can manage this item
        if (!itemService.canUserManageItems(user.getUserId(), responsibilityId, userService)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to manage this item.");
            return "redirect:/manager/items";
        }

        // Update item
        String result = itemService.updateItem(itemId, itemName, status);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Item updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/manager/items";
    }

    /**
     * Delete item
     */
    @PostMapping("/manager/items/delete")
    public String deleteItem(@RequestParam Long itemId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        // Check if user is logged in and is manager
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);
        if (!"manager".equals(user.getRoleName())) {
            return "error/404";
        }

        // Get user's responsibility for permission check
        String responsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (responsibilityName == null) {
            redirectAttributes.addFlashAttribute("error", "You have no responsibility assigned.");
            return "redirect:/manager/items";
        }

        // Get responsibility ID
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findByName(responsibilityName);

        if (!responsibilityOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Responsibility not found.");
            return "redirect:/manager/items";
        }

        Long responsibilityId = responsibilityOptional.get().getResponsibilityId();

        // Verify user can manage this item
        if (!itemService.canUserManageItems(user.getUserId(), responsibilityId, userService)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this item.");
            return "redirect:/manager/items";
        }

        // Delete item
        String result = itemService.deleteItem(itemId);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Item deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/manager/items";
    }

    /**
     * Display responsibility details with item list for all users
     * ENHANCED: Now includes request functionality and event status
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

        // NEW: Add event status data
        boolean canRequestItems = eventService.isResponsibilityInActiveEvent(id);
        boolean canReturnItems = eventService.isResponsibilityInReturnEvent(id);

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("responsibility", responsibility);
        model.addAttribute("items", items);
        model.addAttribute("responsibilityManagers", managers);
        model.addAttribute("userRequestedItemIds", userRequestedItemIds);
        model.addAttribute("canRequestItems", canRequestItems); // NEW: Event status for requests
        model.addAttribute("canReturnItems", canReturnItems);   // NEW: Event status for returns

        return "responsibility-view";
    }

    // ======================
    // USER REQUEST ENDPOINTS
    // ======================

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

        // Users and managers can request items, but not admins/chiefs
        if ("admin".equals(user.getRoleName()) || "chief".equals(user.getRoleName())) {
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

    // ======================
    // NEW: USER ITEMS LIST (MY ITEMS)
    // ======================

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

        // Users and managers can view their items, but not admins/chiefs
        if ("admin".equals(user.getRoleName()) || "chief".equals(user.getRoleName())) {
            return "error/404";
        }

        // Get all items owned by this user
        List<Item> userItems = itemService.getItemsByUserId(user.getUserId());

        // NEW: Check if any events allow returns
        boolean canReturnItems = eventService.areItemReturnsAllowed();

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("userItems", userItems);
        model.addAttribute("itemCount", userItems.size());
        model.addAttribute("canReturnItems", canReturnItems); // NEW: Global return status

        return "user-items";
    }
}