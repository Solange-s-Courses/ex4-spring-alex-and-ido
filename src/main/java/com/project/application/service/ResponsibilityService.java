package com.project.application.service;

import com.project.application.entity.Responsibility;
import com.project.application.entity.User;
import com.project.application.entity.UserResponsibility;
import com.project.application.repository.ResponsibilityRepository;
import com.project.application.repository.UserResponsibilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResponsibilityService {

    private final ResponsibilityRepository responsibilityRepository;
    private final UserResponsibilityRepository userResponsibilityRepository;

    // Get all responsibilities
    public List<Responsibility> getAllResponsibilities() {
        return responsibilityRepository.findAll();
    }

    // Find responsibility by name
    public Optional<Responsibility> findByName(String responsibilityName) {
        return responsibilityRepository.findByResponsibilityName(responsibilityName);
    }

    // Get responsibility by ID
    public Optional<Responsibility> findById(Long responsibilityId) {
        return responsibilityRepository.findById(responsibilityId);
    }

    // Check if responsibility exists by name
    public boolean existsByName(String responsibilityName) {
        return responsibilityRepository.existsByResponsibilityName(responsibilityName);
    }

    // Create new responsibility
    public Responsibility createResponsibility(String responsibilityName) {
        Responsibility responsibility = new Responsibility(responsibilityName.trim());
        return responsibilityRepository.save(responsibility);
    }

    // Delete responsibility
    public void deleteResponsibility(Long responsibilityId) {
        responsibilityRepository.deleteById(responsibilityId);
    }

    // Get all responsibilities with their assigned managers
    public Map<Responsibility, List<User>> getAllResponsibilitiesWithManagers() {
        List<Responsibility> responsibilities = responsibilityRepository.findAll();
        Map<Responsibility, List<User>> responsibilityManagerMap = new HashMap<>();

        for (Responsibility responsibility : responsibilities) {
            List<UserResponsibility> userResponsibilities = userResponsibilityRepository.findByResponsibilityId(responsibility.getResponsibilityId());
            List<User> managers = userResponsibilities.stream()
                    .map(UserResponsibility::getUser)
                    .collect(Collectors.toList());
            responsibilityManagerMap.put(responsibility, managers);
        }

        return responsibilityManagerMap;
    }

    /**
     * Update responsibility description
     */
    @Transactional
    public String updateDescription(Long responsibilityId, String description) {
        try {
            Optional<Responsibility> responsibilityOptional = findById(responsibilityId);
            if (!responsibilityOptional.isPresent()) {
                return "Responsibility not found";
            }

            Responsibility responsibility = responsibilityOptional.get();
            responsibility.setDescription(description);
            responsibilityRepository.save(responsibility);

            return "success";

        } catch (Exception e) {
            return "Failed to update description: " + e.getMessage();
        }
    }
}