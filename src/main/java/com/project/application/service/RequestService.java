package com.project.application.service;

import com.project.application.entity.Request;
import com.project.application.entity.User;
import com.project.application.entity.Item;
import com.project.application.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;
    private final ItemService itemService;

    /**
     * Create a new request (request or return)
     */
    @Transactional
    public String createRequest(Long userId, Long itemId, String requestType) {
        try {
            // Validate input
            if (userId == null || itemId == null || requestType == null) {
                return "Invalid request parameters";
            }

            if (!requestType.equals("request") && !requestType.equals("return")) {
                return "Invalid request type. Must be 'request' or 'return'";
            }

            // Find user
            Optional<User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                return "User not found";
            }

            // Find item
            Optional<Item> itemOptional = itemService.findById(itemId);
            if (!itemOptional.isPresent()) {
                return "Item not found";
            }

            User user = userOptional.get();
            Item item = itemOptional.get();

            // Check if user already has pending request for this item
            if (requestRepository.existsByUser_UserIdAndItem_ItemId(userId, itemId)) {
                return "You already have a pending request for this item";
            }

            // Validate based on request type
            if ("request".equals(requestType)) {
                // For item requests, item must be available
                if (!"Available".equals(item.getStatus())) {
                    return "Item is not available for request";
                }
                if (item.getUser() != null) {
                    return "Item is already owned by another user";
                }
            } else if ("return".equals(requestType)) {
                // For return requests, user must own the item
                if (!"In Use".equals(item.getStatus())) {
                    return "Item is not currently in use";
                }
                if (!item.isOwnedBy(userId)) {
                    return "You don't own this item";
                }
            }

            // Create and save request
            Request request = new Request(user, item, requestType);
            requestRepository.save(request);

            return "success";

        } catch (Exception e) {
            return "Failed to create request: " + e.getMessage();
        }
    }

    /**
     * Get all requests for a specific responsibility (for managers)
     */
    public List<Request> getRequestsByResponsibilityId(Long responsibilityId) {
        return requestRepository.findByResponsibilityId(responsibilityId);
    }

    /**
     * Get all requests by a specific user
     */
    public List<Request> getRequestsByUserId(Long userId) {
        return requestRepository.findByUserId(userId);
    }

    /**
     * Get all requests for a specific item
     */
    public List<Request> getRequestsByItemId(Long itemId) {
        return requestRepository.findByItemId(itemId);
    }

    /**
     * Check if user has pending request for specific item
     */
    public boolean hasUserRequestedItem(Long userId, Long itemId) {
        return requestRepository.existsByUser_UserIdAndItem_ItemId(userId, itemId);
    }

    /**
     * Get specific request by user and item
     */
    public Optional<Request> getRequestByUserAndItem(Long userId, Long itemId) {
        return requestRepository.findByUserIdAndItemId(userId, itemId);
    }

    /**
     * Get request by ID
     */
    public Optional<Request> findById(Long requestId) {
        return requestRepository.findById(requestId);
    }

    /**
     * Approve a request (used by managers)
     */
    @Transactional
    public String approveRequest(Long requestId) {
        try {
            Optional<Request> requestOptional = requestRepository.findById(requestId);
            if (!requestOptional.isPresent()) {
                return "Request not found";
            }

            Request request = requestOptional.get();
            Item item = request.getItem();
            User user = request.getUser();

            if ("request".equals(request.getRequestType())) {
                // For item requests: update item status and owner
                item.setStatus("In Use");
                item.setUser(user);

                // Deny all other pending requests for this item
                List<Request> otherRequests = requestRepository.findByItemId(item.getItemId());
                for (Request otherRequest : otherRequests) {
                    if (!otherRequest.getRequestId().equals(requestId)) {
                        requestRepository.delete(otherRequest);
                    }
                }

            } else if ("return".equals(request.getRequestType())) {
                // For return requests: update item status and remove owner
                item.setStatus("Available");
                item.setUser(null);
            }

            // Save item changes
            itemService.findById(item.getItemId()); // This will trigger save through JPA

            // Delete the approved request (since we only keep pending requests)
            requestRepository.delete(request);

            return "success";

        } catch (Exception e) {
            return "Failed to approve request: " + e.getMessage();
        }
    }

    /**
     * Deny a request (used by managers)
     */
    @Transactional
    public String denyRequest(Long requestId) {
        try {
            Optional<Request> requestOptional = requestRepository.findById(requestId);
            if (!requestOptional.isPresent()) {
                return "Request not found";
            }

            // Simply delete the request (since we only keep pending requests)
            requestRepository.deleteById(requestId);

            return "success";

        } catch (Exception e) {
            return "Failed to deny request: " + e.getMessage();
        }
    }

    /**
     * Count requests for a responsibility
     */
    public long countRequestsByResponsibilityId(Long responsibilityId) {
        return requestRepository.countByResponsibilityId(responsibilityId);
    }

    /**
     * Count requests by user
     */
    public long countRequestsByUserId(Long userId) {
        return requestRepository.countByUserId(userId);
    }

    /**
     * Delete all requests for an item (when item is deleted)
     */
    @Transactional
    public void deleteRequestsByItemId(Long itemId) {
        requestRepository.deleteByItem_ItemId(itemId);
    }

    /**
     * Delete all requests by user (when user is deleted)
     */
    @Transactional
    public void deleteRequestsByUserId(Long userId) {
        requestRepository.deleteByUser_UserId(userId);
    }

    /**
     * Delete all requests for a responsibility (when responsibility is deleted)
     */
    @Transactional
    public void deleteRequestsByResponsibilityId(Long responsibilityId) {
        requestRepository.deleteByResponsibilityId(responsibilityId);
    }

    /**
     * Get requests by type
     */
    public List<Request> getRequestsByType(String requestType) {
        return requestRepository.findByRequestType(requestType);
    }

    /**
     * Get requests by responsibility and type
     */
    public List<Request> getRequestsByResponsibilityIdAndType(Long responsibilityId, String requestType) {
        return requestRepository.findByResponsibilityIdAndRequestType(responsibilityId, requestType);
    }
}