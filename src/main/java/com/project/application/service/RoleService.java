package com.project.application.service;

import com.project.application.entity.Role;
import com.project.application.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    public static final String DEFAULT_ROLE = "user";
    private final RoleRepository roleRepository;

    // Get all roles
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Find role by name
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    // Get role by ID
    public Optional<Role> findById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    // Get default user role
    public Role getDefaultUserRole() {
        return roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new RuntimeException("Default " + DEFAULT_ROLE + " role not found"));
    }

    // Check if role exists
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}