package com.project.application.service;

import com.project.application.entity.Request;
import com.project.application.entity.User;
import com.project.application.entity.Item;
import com.project.application.repository.RequestRepository;
import com.project.application.repository.UserRepository;
import com.project.application.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing item requests and returns in the logistics system.

 * Handles the complete lifecycle of user requests for equipment:
 * - Creating new requests for items (with event status validation)
 * - Managing return requests from users
 * - Approving/denying requests by managers
 * - Cleanup operations for cascading deletions

 * Business Rules:
 * - Item requests only allowed during active events
 * - Return requests allowed during active OR equipment-return events
 * - Users can only have one pending request per item
 * - Only available items can be requested
 * - Only owned items can be returned

 * Note: Uses direct repository access to avoid circular dependencies with UserService
 */
@Service
@RequiredArgsConstructor
public class RequestService {

    // Repository Dependencies
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final EventService eventService;

    // Constants for request types
    private static final String REQUEST_TYPE_REQUEST = "request";
    private static final String REQUEST_TYPE_RETURN = "return";
    private static final String ITEM_STATUS_AVAILABLE = "Available";
    private static final String ITEM_STATUS_IN_USE = "In Use";

    // ========== CORE REQUEST OPERATIONS ==========

    /**
     * Creates a new request for requesting or returning an item.
     * Validates event status, item availability, and user permissions.
     *
     * @param userId The ID of the user making the request
     * @param itemId The ID of the item being requested/returned
     * @param requestType Either "request" or "return"
     * @return "success" if created successfully, error message otherwise
     */
    @Transactional
    public String createRequest(Long userId, Long itemId, String requestType) {
        try {
            // Input validation
            String validationResult = validateRequestInput(userId, itemId, requestType);
            if (!validationResult.equals("success")) {
                return validationResult;
            }

            // Fetch entities
            User user = getUserById(userId);
            Item item = getItemById(itemId);
            if (user == null) return "User not found";
            if (item == null) return "Item not found";

            // Check for duplicate requests
            if (hasPendingRequest(userId, itemId)) {
                return "You already have a pending request for this item";
            }

            // Validate business rules based on request type
            String businessValidationResult = validateBusinessRules(user, item, requestType);
            if (!businessValidationResult.equals("success")) {
                return businessValidationResult;
            }

            // Create and save the request
            Request request = new Request(user, item, requestType);
            requestRepository.save(request);

            return "success";

        } catch (Exception e) {
            return "Failed to create request: " + e.getMessage();
        }
    }

    /**
     * Approves a request and updates item status accordingly.
     * For item requests: assigns item to user and sets status to "In Use"
     * For return requests: removes user assignment and sets status to "Available"
     *
     * @param requestId The ID of the request to approve
     * @return "success" if approved successfully, error message otherwise
     */
    @Transactional
    public String approveRequest(Long requestId) {
        try {
            Optional<Request> requestOptional = requestRepository.findById(requestId);
            if (requestOptional.isEmpty()) {
                return "Request not found";
            }

            Request request = requestOptional.get();
            Item item = request.getItem();
            User user = request.getUser();

            if (REQUEST_TYPE_REQUEST.equals(request.getRequestType())) {
                processItemRequest(item, user);
            } else if (REQUEST_TYPE_RETURN.equals(request.getRequestType())) {
                processItemReturn(item);
            }

            // Save item changes and remove the approved request
            itemRepository.save(item);
            requestRepository.delete(request);

            return "success";

        } catch (Exception e) {
            return "Failed to approve request: " + e.getMessage();
        }
    }

    /**
     * Denies a request by simply removing it from the system.
     *
     * @param requestId The ID of the request to deny
     * @return "success" if denied successfully, error message otherwise
     */
    @Transactional
    public String denyRequest(Long requestId) {
        try {
            if (!requestRepository.existsById(requestId)) {
                return "Request not found";
            }

            requestRepository.deleteById(requestId);
            return "success";

        } catch (Exception e) {
            return "Failed to deny request: " + e.getMessage();
        }
    }

    // ========== QUERY OPERATIONS ==========

    /**
     * Retrieves all requests for a specific responsibility (used by managers).
     */
    public List<Request> getRequestsByResponsibilityId(Long responsibilityId) {
        return requestRepository.findByResponsibilityId(responsibilityId);
    }

    /**
     * Retrieves all requests made by a specific user.
     */
    public List<Request> getRequestsByUserId(Long userId) {
        return requestRepository.findByUserId(userId);
    }

    /**
     * Retrieves all requests for a specific item.
     */
    public List<Request> getRequestsByItemId(Long itemId) {
        return requestRepository.findByItemId(itemId);
    }

    /**
     * Retrieves all requests of a specific type ("request" or "return").
     */
    public List<Request> getRequestsByType(String requestType) {
        return requestRepository.findByRequestType(requestType);
    }

    /**
     * Retrieves requests for a responsibility filtered by type.
     */
    public List<Request> getRequestsByResponsibilityIdAndType(Long responsibilityId, String requestType) {
        return requestRepository.findByResponsibilityIdAndRequestType(responsibilityId, requestType);
    }

    /**
     * Gets a specific request by ID.
     */
    public Optional<Request> findById(Long requestId) {
        return requestRepository.findById(requestId);
    }

    /**
     * Gets a specific request by user and item combination.
     */
    public Optional<Request> getRequestByUserAndItem(Long userId, Long itemId) {
        return requestRepository.findByUserIdAndItemId(userId, itemId);
    }

    // ========== UTILITY METHODS ==========

    /**
     * Checks if a user has a pending request for a specific item.
     */
    public boolean hasUserRequestedItem(Long userId, Long itemId) {
        return requestRepository.existsByUser_UserIdAndItem_ItemId(userId, itemId);
    }

    /**
     * Counts requests for a specific responsibility.
     */
    public long countRequestsByResponsibilityId(Long responsibilityId) {
        return requestRepository.countByResponsibilityId(responsibilityId);
    }

    /**
     * Counts requests made by a specific user.
     */
    public long countRequestsByUserId(Long userId) {
        return requestRepository.countByUserId(userId);
    }

    // ========== CLEANUP OPERATIONS (for cascading deletions) ==========

    /**
     * Deletes all requests for a specific item (used when item is deleted).
     */
    @Transactional
    public void deleteRequestsByItemId(Long itemId) {
        requestRepository.deleteByItem_ItemId(itemId);
    }

    /**
     * Deletes all requests made by a specific user (used when user is deleted).
     */
    @Transactional
    public void deleteRequestsByUserId(Long userId) {
        requestRepository.deleteByUser_UserId(userId);
    }

    /**
     * Deletes all requests for items in a responsibility (used when responsibility is deleted).
     */
    @Transactional
    public void deleteRequestsByResponsibilityId(Long responsibilityId) {
        requestRepository.deleteByResponsibilityId(responsibilityId);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Validates basic input parameters for request creation.
     */
    private String validateRequestInput(Long userId, Long itemId, String requestType) {
        if (userId == null || itemId == null || requestType == null) {
            return "Invalid request parameters";
        }

        if (!REQUEST_TYPE_REQUEST.equals(requestType) && !REQUEST_TYPE_RETURN.equals(requestType)) {
            return "Invalid request type. Must be 'request' or 'return'";
        }

        return "success";
    }

    /**
     * Validates business rules for request creation based on request type.
     */
    private String validateBusinessRules(User user, Item item, String requestType) {
        Long responsibilityId = item.getResponsibilityId();

        if (REQUEST_TYPE_REQUEST.equals(requestType)) {
            return validateItemRequestRules(item, responsibilityId);
        } else if (REQUEST_TYPE_RETURN.equals(requestType)) {
            return validateItemReturnRules(item, user.getUserId(), responsibilityId);
        }

        return "success";
    }

    /**
     * Validates rules specific to item requests.
     */
    private String validateItemRequestRules(Item item, Long responsibilityId) {
        // Check event status for item requests
        if (!eventService.isResponsibilityInActiveEvent(responsibilityId)) {
            return "Item requests are not allowed at this time. No active events for this responsibility.";
        }

        // Check item availability
        if (!ITEM_STATUS_AVAILABLE.equals(item.getStatus())) {
            return "Item is not available for request";
        }

        if (item.getUser() != null) {
            return "Item is already owned by another user";
        }

        return "success";
    }

    /**
     * Validates rules specific to item returns.
     */
    private String validateItemReturnRules(Item item, Long userId, Long responsibilityId) {
        // Check event status for returns (active OR return-mode events allowed)
        if (!eventService.isResponsibilityInReturnAllowedEvent(responsibilityId)) {
            return "Item returns are not allowed at this time. No active or return-mode events for this responsibility.";
        }

        // Check item ownership
        if (!ITEM_STATUS_IN_USE.equals(item.getStatus())) {
            return "Item is not currently in use";
        }

        if (!item.isOwnedBy(userId)) {
            return "You don't own this item";
        }

        return "success";
    }

    /**
     * Processes an approved item request by assigning the item to the user.
     */
    private void processItemRequest(Item item, User user) {
        item.setStatus(ITEM_STATUS_IN_USE);
        item.setUser(user);

        // Deny all other pending requests for this item
        List<Request> otherRequests = requestRepository.findByItemId(item.getItemId());
        for (Request otherRequest : otherRequests) {
            if (!otherRequest.getRequestId().equals(item.getItemId())) {
                requestRepository.delete(otherRequest);
            }
        }
    }

    /**
     * Processes an approved item return by removing user assignment.
     */
    private void processItemReturn(Item item) {
        item.setStatus(ITEM_STATUS_AVAILABLE);
        item.setUser(null);
    }

    /**
     * Helper method to check if user has pending request for item.
     */
    private boolean hasPendingRequest(Long userId, Long itemId) {
        return requestRepository.existsByUser_UserIdAndItem_ItemId(userId, itemId);
    }

    /**
     * Helper method to get user by ID with null safety.
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Helper method to get item by ID with null safety.
     */
    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId).orElse(null);
    }
}