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
     * Display responsibility management page for managers
     * UPDATED: Now accepts responsibilityId parameter and fetches managers + handles flash messages
     */
    @GetMapping("/responsibility-manage/{responsibilityId}")
    public String responsibilityManagement(@PathVariable Long responsibilityId,
                                           HttpSession session,
                                           Model model) {
        // Check if user is logged in
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);

        // Check if user is manager
        if (!"manager".equals(user.getRoleName())) {
            return "error/404";
        }

        // Get responsibility details
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findById(responsibilityId);

        if (!responsibilityOptional.isPresent()) {
            model.addAttribute("error", "Responsibility not found in system.");
            model.addAttribute("user", user);
            return "error/404";
        }

        com.project.application.entity.Responsibility responsibility = responsibilityOptional.get();

        // Check if the current manager is assigned to this responsibility
        String userResponsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (!responsibility.getResponsibilityName().equals(userResponsibilityName)) {
            model.addAttribute("error", "You don't have permission to manage this responsibility.");
            model.addAttribute("user", user);
            return "error/404";
        }

        // Get all items for this responsibility
        List<Item> items = itemService.getItemsByResponsibilityId(responsibilityId);

        // FIXED: Get managers for this responsibility
        List<User> responsibilityManagers = userService.getResponsibilityManagers(responsibilityId);

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("responsibility", responsibility);
        model.addAttribute("items", items);
        model.addAttribute("itemCount", items.size());
        model.addAttribute("responsibilityManagers", responsibilityManagers);

        return "responsibility-manage";
    }
    // ======================
    // MANAGER REQUEST MANAGEMENT
    // ======================

    /**
     * Display manager requests page
     * UPDATED: Now uses responsibilityId parameter
     */
    @GetMapping("/responsibility-manage/{responsibilityId}/requests")
    public String managerRequests(@PathVariable Long responsibilityId,
                                  HttpSession session,
                                  Model model) {
        // Check if user is logged in
        if (!isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);

        // Check if user is manager
        if (!"manager".equals(user.getRoleName())) {
            return "error/404";
        }

        // Get responsibility details
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findById(responsibilityId);

        if (!responsibilityOptional.isPresent()) {
            model.addAttribute("error", "Responsibility not found in system.");
            model.addAttribute("user", user);
            return "error/404";
        }

        com.project.application.entity.Responsibility responsibility = responsibilityOptional.get();

        // Check if the current manager is assigned to this responsibility
        String userResponsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (!responsibility.getResponsibilityName().equals(userResponsibilityName)) {
            model.addAttribute("error", "You don't have permission to manage this responsibility.");
            model.addAttribute("user", user);
            return "error/404";
        }

        // Get all pending requests for this responsibility
        List<Request> pendingRequests = requestService.getRequestsByResponsibilityId(responsibilityId);

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("responsibility", responsibility);
        model.addAttribute("requests", pendingRequests);
        model.addAttribute("requestCount", pendingRequests.size());

        return "manager-requests";
    }

    /**
     * Approve a request
     * UPDATED: Now includes responsibilityId in URL for consistency
     */
    @PostMapping("/responsibility-manage/{responsibilityId}/approve-request")
    @ResponseBody
    public String approveRequest(@PathVariable Long responsibilityId,
                                 @RequestParam Long requestId,
                                 HttpSession session) {

        // Check if user is logged in and is manager
        if (!isUserLoggedIn(session)) {
            return "error:Please log in to approve requests";
        }

        User user = getLoggedInUser(session);
        if (!"manager".equals(user.getRoleName())) {
            return "error:Access denied";
        }

        // Verify responsibility exists and user has permission
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findById(responsibilityId);

        if (!responsibilityOptional.isPresent()) {
            return "error:Responsibility not found";
        }

        // Check if the current manager is assigned to this responsibility
        String userResponsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (!responsibilityOptional.get().getResponsibilityName().equals(userResponsibilityName)) {
            return "error:You don't have permission to manage this responsibility";
        }

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
     * UPDATED: Now includes responsibilityId in URL for consistency
     */
    @PostMapping("/responsibility-manage/{responsibilityId}/deny-request")
    @ResponseBody
    public String denyRequest(@PathVariable Long responsibilityId,
                              @RequestParam Long requestId,
                              HttpSession session) {

        // Check if user is logged in and is manager
        if (!isUserLoggedIn(session)) {
            return "error:Please log in to deny requests";
        }

        User user = getLoggedInUser(session);
        if (!"manager".equals(user.getRoleName())) {
            return "error:Access denied";
        }

        // Verify responsibility exists and user has permission
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findById(responsibilityId);

        if (!responsibilityOptional.isPresent()) {
            return "error:Responsibility not found";
        }

        // Check if the current manager is assigned to this responsibility
        String userResponsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (!responsibilityOptional.get().getResponsibilityName().equals(userResponsibilityName)) {
            return "error:You don't have permission to manage this responsibility";
        }

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
     * UPDATED: Now includes responsibilityId in URL and redirect
     */
    @PostMapping("/responsibility-manage/{responsibilityId}/update-description")
    public String updateDescription(@PathVariable Long responsibilityId,
                                    @RequestParam String description,
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

        // Get responsibility
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findById(responsibilityId);

        if (!responsibilityOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Responsibility not found.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        com.project.application.entity.Responsibility responsibility = responsibilityOptional.get();

        // Check if the current manager is assigned to this responsibility
        String userResponsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (!responsibility.getResponsibilityName().equals(userResponsibilityName)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to manage this responsibility.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Validate description
        if (description != null) {
            description = description.trim();

            if (description.length() > 200) {
                redirectAttributes.addFlashAttribute("error", "Description cannot exceed 200 characters.");
                return "redirect:/responsibility-manage/" + responsibilityId;
            }

            // If description is empty, set to null
            if (description.isEmpty()) {
                description = null;
            }
        }

        // Update description using service
        String result = responsibilityService.updateDescription(responsibilityId, description);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Description updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/responsibility-manage/" + responsibilityId;
    }

    /**
     * Add new item
     * UPDATED: Now includes responsibilityId in URL and redirect
     */
    @PostMapping("/responsibility-manage/{responsibilityId}/add-item")
    public String addItem(@PathVariable Long responsibilityId,
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

        // Verify responsibility exists and user has permission
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findById(responsibilityId);

        if (!responsibilityOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Responsibility not found.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Check if the current manager is assigned to this responsibility
        String userResponsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (!responsibilityOptional.get().getResponsibilityName().equals(userResponsibilityName)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to manage this responsibility.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Create item
        String result = itemService.createItem(responsibilityId, itemName, status);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Item added successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/responsibility-manage/" + responsibilityId;
    }

    /**
     * Update existing item
     * UPDATED: Now includes responsibilityId in URL and redirect
     */
    @PostMapping("/responsibility-manage/{responsibilityId}/update-item")
    public String updateItem(@PathVariable Long responsibilityId,
                             @RequestParam Long itemId,
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

        // Verify responsibility exists and user has permission
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findById(responsibilityId);

        if (!responsibilityOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Responsibility not found.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Check if the current manager is assigned to this responsibility
        String userResponsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (!responsibilityOptional.get().getResponsibilityName().equals(userResponsibilityName)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to manage this responsibility.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Verify user can manage this item
        if (!itemService.canUserManageItems(user.getUserId(), responsibilityId, userService)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to manage this item.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Check if item is in use before updating
        Optional<Item> itemOptional = itemService.findById(itemId);
        if (!itemOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Item not found.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        Item item = itemOptional.get();
        if (item.isInUse()) {
            redirectAttributes.addFlashAttribute("error", "Cannot edit item that is currently in use.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Update item
        String result = itemService.updateItem(itemId, itemName, status);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Item updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/responsibility-manage/" + responsibilityId;
    }

    /**
     * Delete item
     * UPDATED: Now includes responsibilityId in URL and redirect
     */
    @PostMapping("/responsibility-manage/{responsibilityId}/delete-item")
    public String deleteItem(@PathVariable Long responsibilityId,
                             @RequestParam Long itemId,
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

        // Verify responsibility exists and user has permission
        Optional<com.project.application.entity.Responsibility> responsibilityOptional =
                responsibilityService.findById(responsibilityId);

        if (!responsibilityOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Responsibility not found.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Check if the current manager is assigned to this responsibility
        String userResponsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (!responsibilityOptional.get().getResponsibilityName().equals(userResponsibilityName)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to manage this responsibility.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Verify user can manage this item
        if (!itemService.canUserManageItems(user.getUserId(), responsibilityId, userService)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this item.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Check if item is in use before deleting
        Optional<Item> itemOptional = itemService.findById(itemId);
        if (!itemOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Item not found.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        Item item = itemOptional.get();
        if (item.isInUse()) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete item that is currently in use.");
            return "redirect:/responsibility-manage/" + responsibilityId;
        }

        // Delete item
        String result = itemService.deleteItem(itemId);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Item deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/responsibility-manage/" + responsibilityId;
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

        // Add event status data
        boolean canRequestItems = eventService.isResponsibilityInActiveEvent(id);
        boolean canReturnItems = eventService.isResponsibilityInReturnEvent(id);

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("responsibility", responsibility);
        model.addAttribute("items", items);
        model.addAttribute("responsibilityManagers", managers);
        model.addAttribute("userRequestedItemIds", userRequestedItemIds);
        model.addAttribute("canRequestItems", canRequestItems);
        model.addAttribute("canReturnItems", canReturnItems);

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

    // ======================
    // USER ITEMS LIST (MY ITEMS)
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

        // Users, managers, and chiefs can view their items, but not admins
        if ("admin".equals(user.getRoleName())) {
            return "error/404";
        }

        // Get all items owned by this user
        List<Item> userItems = itemService.getItemsByUserId(user.getUserId());

        // Check if any events allow returns
        boolean canReturnItems = eventService.areItemReturnsAllowed();

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("userItems", userItems);
        model.addAttribute("itemCount", userItems.size());
        model.addAttribute("canReturnItems", canReturnItems);

        return "user-items";
    }
}