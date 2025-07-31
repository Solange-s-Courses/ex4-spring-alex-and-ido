package com.project.application.controller;

import com.project.application.entity.Item;
import com.project.application.entity.User;
import com.project.application.entity.Request;
import com.project.application.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ResponsibilityManageController {

    private final ItemService itemService;
    private final UserService userService;
    private final ResponsibilityService responsibilityService;
    private final RequestService requestService;

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

        // Get managers for this responsibility
        List<User> responsibilityManagers = userService.getResponsibilityManagers(responsibilityId);

        // Get all pending requests for this responsibility
        List<Request> pendingRequests = requestService.getRequestsByResponsibilityId(responsibilityId);

        // Add data to model
        model.addAttribute("user", user);
        model.addAttribute("responsibility", responsibility);
        model.addAttribute("items", items);
        model.addAttribute("itemCount", items.size());
        model.addAttribute("responsibilityManagers", responsibilityManagers);
        model.addAttribute("requests", pendingRequests);
        model.addAttribute("requestCount", pendingRequests.size());
        model.addAttribute("activeNavButton", "responsibility");

        return "responsibility-manage";
    }

    /**
     * Approve a request
     * UPDATED: Now uses redirect with flash attributes instead of AJAX response and handles tab parameter
     */
    @PostMapping("/responsibility-manage/{responsibilityId}/approve-request")
    public String approveRequest(@PathVariable Long responsibilityId,
                                 @RequestParam Long requestId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request) {

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
            String activeTab = request.getParameter("activeTab");
            if ("requests".equals(activeTab)) {
                return "redirect:/responsibility-manage/" + responsibilityId + "?tab=requests";
            } else {
                return "redirect:/responsibility-manage/" + responsibilityId;
            }
        }

        // Check if the current manager is assigned to this responsibility
        String userResponsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (!responsibilityOptional.get().getResponsibilityName().equals(userResponsibilityName)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to manage this responsibility.");
            String activeTab = request.getParameter("activeTab");
            if ("requests".equals(activeTab)) {
                return "redirect:/responsibility-manage/" + responsibilityId + "?tab=requests";
            } else {
                return "redirect:/responsibility-manage/" + responsibilityId;
            }
        }

        // Verify that this request belongs to the manager's responsibility
        Optional<Request> requestOptional = requestService.findById(requestId);
        if (!requestOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Request not found.");
            String activeTab = request.getParameter("activeTab");
            if ("requests".equals(activeTab)) {
                return "redirect:/responsibility-manage/" + responsibilityId + "?tab=requests";
            } else {
                return "redirect:/responsibility-manage/" + responsibilityId;
            }
        }

        Request requestEntity = requestOptional.get();
        if (!requestEntity.getResponsibilityId().equals(responsibilityId)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to manage this request.");
            String activeTab = request.getParameter("activeTab");
            if ("requests".equals(activeTab)) {
                return "redirect:/responsibility-manage/" + responsibilityId + "?tab=requests";
            } else {
                return "redirect:/responsibility-manage/" + responsibilityId;
            }
        }

        // Approve the request
        String result = requestService.approveRequest(requestId);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Request approved successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        // Handle tab parameter for redirect
        String activeTab = request.getParameter("activeTab");
        if ("requests".equals(activeTab)) {
            return "redirect:/responsibility-manage/" + responsibilityId + "?tab=requests";
        } else {
            return "redirect:/responsibility-manage/" + responsibilityId;
        }
    }

    /**
     * Deny a request
     * UPDATED: Now uses redirect with flash attributes instead of AJAX response and handles tab parameter
     */
    @PostMapping("/responsibility-manage/{responsibilityId}/deny-request")
    public String denyRequest(@PathVariable Long responsibilityId,
                              @RequestParam Long requestId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request) {

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
            String activeTab = request.getParameter("activeTab");
            if ("requests".equals(activeTab)) {
                return "redirect:/responsibility-manage/" + responsibilityId + "?tab=requests";
            } else {
                return "redirect:/responsibility-manage/" + responsibilityId;
            }
        }

        // Check if the current manager is assigned to this responsibility
        String userResponsibilityName = userService.getUserResponsibilityName(user.getUserId());
        if (!responsibilityOptional.get().getResponsibilityName().equals(userResponsibilityName)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to manage this responsibility.");
            String activeTab = request.getParameter("activeTab");
            if ("requests".equals(activeTab)) {
                return "redirect:/responsibility-manage/" + responsibilityId + "?tab=requests";
            } else {
                return "redirect:/responsibility-manage/" + responsibilityId;
            }
        }

        // Verify that this request belongs to the manager's responsibility
        Optional<Request> requestOptional = requestService.findById(requestId);
        if (!requestOptional.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Request not found.");
            String activeTab = request.getParameter("activeTab");
            if ("requests".equals(activeTab)) {
                return "redirect:/responsibility-manage/" + responsibilityId + "?tab=requests";
            } else {
                return "redirect:/responsibility-manage/" + responsibilityId;
            }
        }

        Request requestEntity = requestOptional.get();
        if (!requestEntity.getResponsibilityId().equals(responsibilityId)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to manage this request.");
            String activeTab = request.getParameter("activeTab");
            if ("requests".equals(activeTab)) {
                return "redirect:/responsibility-manage/" + responsibilityId + "?tab=requests";
            } else {
                return "redirect:/responsibility-manage/" + responsibilityId;
            }
        }

        // Deny the request
        String result = requestService.denyRequest(requestId);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Request denied successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        // Handle tab parameter for redirect
        String activeTab = request.getParameter("activeTab");
        if ("requests".equals(activeTab)) {
            return "redirect:/responsibility-manage/" + responsibilityId + "?tab=requests";
        } else {
            return "redirect:/responsibility-manage/" + responsibilityId;
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

        // Delete all pending requests for this item first
        requestService.deleteRequestsByItemId(itemId);

        // Then delete the item
        String result = itemService.deleteItem(itemId);

        if ("success".equals(result)) {
            redirectAttributes.addFlashAttribute("success", "Item deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", result);
        }

        return "redirect:/responsibility-manage/" + responsibilityId;
    }
}