package com.project.application.service;

import com.project.application.entity.Responsibility;
import com.project.application.entity.User;
import com.project.application.entity.UserResponsibility;
import com.project.application.repository.ResponsibilityRepository;
import com.project.application.repository.UserResponsibilityRepository;
import com.project.application.repository.EventResponsibilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Service layer for managing responsibilities in the logistics system.

 * Handles the complete lifecycle of responsibilities including:
 * - Creating and managing responsibility entities
 * - Tracking responsibility-manager assignments
 * - Managing responsibility-event relationships
 * - Safe cascade deletion with foreign key constraint handling
 * - Querying responsibilities with associated managers

 * Business Rules:
 * - Responsibilities can have multiple managers assigned
 * - Responsibilities are linked to events through event-responsibility relationships
 * - When deleting responsibilities, all related data must be cleaned up in proper order
 * - Responsibility names must be unique within the system

 * Foreign Key Dependencies (deletion order):
 * 1. Event-responsibility relationships (prevents FK constraint violations)
 * 2. User-responsibility assignments
 * 3. Responsibility entity itself
 */
@Service
@RequiredArgsConstructor
public class ResponsibilityService {

    // Repository Dependencies
    private final ResponsibilityRepository responsibilityRepository;
    private final UserResponsibilityRepository userResponsibilityRepository;
    private final EventResponsibilityRepository eventResponsibilityRepository;

    // ========== RESPONSIBILITY CRUD OPERATIONS ==========

    /**
     * Creates a new responsibility with the specified name.
     * Automatically trims the responsibility name.
     *
     * @param responsibilityName The name for the new responsibility
     * @return The created Responsibility entity
     */
    public Responsibility createResponsibility(String responsibilityName) {
        Responsibility responsibility = new Responsibility(responsibilityName.trim());
        return responsibilityRepository.save(responsibility);
    }

    /**
     * Deletes a responsibility with complete cascade cleanup.
     * Handles foreign key constraints by deleting related data in proper order:
     * 1. Event-responsibility relationships (prevents FK violations)
     * 2. User-responsibility assignments (removes manager assignments)
     * 3. Responsibility entity (safe to delete after cleanup)
     *
     * @param responsibilityId The ID of the responsibility to delete
     * @throws RuntimeException if deletion fails
     */
    @Transactional
    public void deleteResponsibility(Long responsibilityId) {
        try {
            // Step 1: Remove from events first (critical for FK constraint resolution)
            cleanupEventResponsibilityRelationships(responsibilityId);

            // Step 2: Remove manager assignments (cleanup user assignments)
            cleanupUserResponsibilityAssignments(responsibilityId);

            // Step 3: Safe to delete responsibility entity
            responsibilityRepository.deleteById(responsibilityId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete responsibility: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the description of an existing responsibility.
     *
     * @param responsibilityId The ID of the responsibility to update
     * @param description The new description text
     * @return "success" if updated successfully, error message otherwise
     */
    @Transactional
    public String updateDescription(Long responsibilityId, String description) {
        try {
            Responsibility responsibility = getResponsibilityOrFail(responsibilityId);
            if (responsibility == null) {
                return "Responsibility not found";
            }

            responsibility.setDescription(description);
            responsibilityRepository.save(responsibility);
            return "success";

        } catch (Exception e) {
            return "Failed to update description: " + e.getMessage();
        }
    }

    // ========== QUERY OPERATIONS ==========

    /**
     * Retrieves all responsibilities in the system.
     */
    public List<Responsibility> getAllResponsibilities() {
        return responsibilityRepository.findAll();
    }

    /**
     * Finds a responsibility by its exact name.
     *
     * @param responsibilityName The name to search for
     * @return Optional containing the responsibility if found
     */
    public Optional<Responsibility> findByName(String responsibilityName) {
        return responsibilityRepository.findByResponsibilityName(responsibilityName);
    }

    /**
     * Finds a responsibility by its ID.
     *
     * @param responsibilityId The ID to search for
     * @return Optional containing the responsibility if found
     */
    public Optional<Responsibility> findById(Long responsibilityId) {
        return responsibilityRepository.findById(responsibilityId);
    }

    /**
     * Checks if a responsibility with the given name already exists.
     *
     * @param responsibilityName The name to check
     * @return true if a responsibility with this name exists
     */
    public boolean existsByName(String responsibilityName) {
        return responsibilityRepository.existsByResponsibilityName(responsibilityName);
    }

    /**
     * Retrieves all responsibilities with their assigned managers.
     * Returns a map where keys are Responsibility entities and values are lists of assigned Users.
     *
     * @return Map of responsibilities to their assigned manager lists
     */
    public Map<Responsibility, List<User>> getAllResponsibilitiesWithManagers() {
        List<Responsibility> responsibilities = getAllResponsibilities();
        Map<Responsibility, List<User>> responsibilityManagerMap = new HashMap<>();

        for (Responsibility responsibility : responsibilities) {
            List<User> managers = getManagersForResponsibility(responsibility.getResponsibilityId());
            responsibilityManagerMap.put(responsibility, managers);
        }

        return responsibilityManagerMap;
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Gets a responsibility by ID with null safety.
     *
     * @param responsibilityId The ID to look up
     * @return Responsibility entity or null if not found
     */
    private Responsibility getResponsibilityOrFail(Long responsibilityId) {
        return findById(responsibilityId).orElse(null);
    }

    /**
     * Retrieves all managers assigned to a specific responsibility.
     *
     * @param responsibilityId The responsibility ID
     * @return List of User entities assigned as managers
     */
    private List<User> getManagersForResponsibility(Long responsibilityId) {
        List<UserResponsibility> userResponsibilities =
                userResponsibilityRepository.findByResponsibilityId(responsibilityId);

        return userResponsibilities.stream()
                .map(UserResponsibility::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Cleans up event-responsibility relationships for cascade deletion.
     * This step is critical to prevent foreign key constraint violations.
     *
     * @param responsibilityId The responsibility ID to clean up
     */
    private void cleanupEventResponsibilityRelationships(Long responsibilityId) {
        eventResponsibilityRepository.deleteByResponsibility_ResponsibilityId(responsibilityId);
    }

    /**
     * Cleans up user-responsibility assignments for cascade deletion.
     * Removes all manager assignments for the responsibility.
     *
     * @param responsibilityId The responsibility ID to clean up
     */
    private void cleanupUserResponsibilityAssignments(Long responsibilityId) {
        userResponsibilityRepository.deleteByResponsibility_ResponsibilityId(responsibilityId);
    }
}