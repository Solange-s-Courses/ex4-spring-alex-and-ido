package com.project.application.service;

import com.project.application.entity.Item;
import com.project.application.entity.Responsibility;
import com.project.application.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final String ITEM_NAME_PATTERN = "^[A-Za-z0-9 ]{1,32}$";

    private final ItemRepository itemRepository;
    private final ResponsibilityService responsibilityService;

    /**
     * Get all items for a specific responsibility
     */
    public List<Item> getItemsByResponsibilityId(Long responsibilityId) {
        return itemRepository.findByResponsibilityId(responsibilityId);
    }

    /**
     * Create a new item within a responsibility
     */
    @Transactional
    public String createItem(Long responsibilityId, String itemName, String status) {
        try {
            // Validate input
            if (itemName == null || itemName.trim().isEmpty()) {
                return "Item name cannot be empty";
            }

            if (status == null || status.trim().isEmpty()) {
                return "Status cannot be empty";
            }

            // Trim and validate item name
            itemName = itemName.trim();
            status = status.trim();

            // Validate item name pattern
            if (!itemName.matches(ITEM_NAME_PATTERN)) {
                return "Item name must contain only letters, numbers, and spaces, max 32 characters";
            }

            // Find responsibility
            Optional<Responsibility> responsibilityOptional = responsibilityService.findById(responsibilityId);
            if (!responsibilityOptional.isPresent()) {
                return "Responsibility not found";
            }

            Responsibility responsibility = responsibilityOptional.get();

            // Check if item name already exists in this responsibility
            if (itemRepository.existsByItemNameAndResponsibility_ResponsibilityId(itemName, responsibilityId)) {
                return "Item name already exists in this responsibility";
            }

            // Create and save item
            Item item = new Item(itemName, status, responsibility);
            itemRepository.save(item);

            return "success";

        } catch (Exception e) {
            return "Failed to create item: " + e.getMessage();
        }
    }

    /**
     * Update an existing item
     */
    @Transactional
    public String updateItem(Long itemId, String itemName, String status) {
        try {
            // Validate input
            if (itemName == null || itemName.trim().isEmpty()) {
                return "Item name cannot be empty";
            }

            if (status == null || status.trim().isEmpty()) {
                return "Status cannot be empty";
            }

            // Trim inputs
            itemName = itemName.trim();
            status = status.trim();

            // Validate item name pattern
            if (!itemName.matches(ITEM_NAME_PATTERN)) {
                return "Item name must contain only letters and spaces, max 32 characters";
            }

            // Find existing item
            Optional<Item> itemOptional = itemRepository.findById(itemId);
            if (!itemOptional.isPresent()) {
                return "Item not found";
            }

            Item item = itemOptional.get();
            Long responsibilityId = item.getResponsibilityId();

            // Check if new name conflicts with existing items (excluding current item)
            Optional<Item> conflictingItem = itemRepository.findByItemNameAndResponsibilityId(itemName, responsibilityId);
            if (conflictingItem.isPresent() && !conflictingItem.get().getItemId().equals(itemId)) {
                return "Item name already exists in this responsibility";
            }

            // Update item
            item.setItemName(itemName);
            item.setStatus(status);
            itemRepository.save(item);

            return "success";

        } catch (Exception e) {
            return "Failed to update item: " + e.getMessage();
        }
    }

    /**
     * Delete an item
     */
    @Transactional
    public String deleteItem(Long itemId) {
        try {
            Optional<Item> itemOptional = itemRepository.findById(itemId);
            if (!itemOptional.isPresent()) {
                return "Item not found";
            }

            itemRepository.deleteById(itemId);
            return "success";

        } catch (Exception e) {
            return "Failed to delete item: " + e.getMessage();
        }
    }

    /**
     * Get item by ID
     */
    public Optional<Item> findById(Long itemId) {
        return itemRepository.findById(itemId);
    }

    /**
     * Get items by responsibility and status
     */
    public List<Item> getItemsByResponsibilityIdAndStatus(Long responsibilityId, String status) {
        return itemRepository.findByResponsibilityIdAndStatus(responsibilityId, status);
    }

    /**
     * Count items in a responsibility
     */
    public long countItemsByResponsibilityId(Long responsibilityId) {
        return itemRepository.countByResponsibilityId(responsibilityId);
    }

    /**
     * Delete all items for a responsibility (called when responsibility is deleted)
     */
    @Transactional
    public void deleteAllItemsByResponsibilityId(Long responsibilityId) {
        itemRepository.deleteByResponsibility_ResponsibilityId(responsibilityId);
    }

    /**
     * Check if user has permission to manage items (must be manager with assigned responsibility)
     */
    public boolean canUserManageItems(Long userId, Long responsibilityId, UserService userService) {
        try {
            // Get user
            Optional<com.project.application.entity.User> userOptional = userService.findById(userId);
            if (!userOptional.isPresent()) {
                return false;
            }

            com.project.application.entity.User user = userOptional.get();

            // Check if user is manager
            if (!"manager".equals(user.getRoleName())) {
                return false;
            }

            // Check if user's responsibility matches the requested responsibility
            String userResponsibilityName = userService.getUserResponsibilityName(userId);
            if (userResponsibilityName == null) {
                return false;
            }

            // Find responsibility by ID and check if names match
            Optional<Responsibility> responsibilityOptional = responsibilityService.findById(responsibilityId);
            if (!responsibilityOptional.isPresent()) {
                return false;
            }

            return responsibilityOptional.get().getResponsibilityName().equals(userResponsibilityName);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get all items owned by a specific user
     */
    public List<Item> getItemsByUserId(Long userId) {
        return itemRepository.findByUser_UserId(userId);
    }

    /**
     * Get count of items owned by a specific user
     */
    public long countItemsByUserId(Long userId) {
        return itemRepository.countByUser_UserId(userId);
    }

    /**
     * Check if an item has pending requests and return appropriate message
     */
    public String getItemRequestStatus(Long itemId, List<com.project.application.entity.Request> allRequests) {
        if (allRequests == null || allRequests.isEmpty()) {
            return null; // No requests
        }

        // Check if this item has any pending requests
        boolean hasRequestRequests = false;
        boolean hasReturnRequests = false;

        for (com.project.application.entity.Request request : allRequests) {
            if (request.getItemId().equals(itemId)) {
                if ("request".equals(request.getRequestType())) {
                    hasRequestRequests = true;
                } else if ("return".equals(request.getRequestType())) {
                    hasReturnRequests = true;
                }
            }
        }

        // Return appropriate message
        if (hasReturnRequests) {
            return "Return pending";
        } else if (hasRequestRequests) {
            return "Item being requested";
        }

        return null; // No requests for this item
    }
}