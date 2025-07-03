package com.project.application.service;

import com.project.application.entity.Responsibility;
import com.project.application.repository.ResponsibilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResponsibilityService {

    private final ResponsibilityRepository responsibilityRepository;

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
}