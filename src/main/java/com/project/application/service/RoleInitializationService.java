package com.project.application.service;

import com.project.application.entity.Role;
import com.project.application.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleInitializationService {

    private static final String[] PREDEFINED_ROLES = {"admin", "chief", "manager", "user"};
    private final RoleRepository roleRepository;

    // This runs automatically when the application starts
    @Bean
    public ApplicationRunner initializeRoles() {
        return args -> {
            boolean roleAdded = false;

            // Check if any roles are missing and create them
            for (String roleName : PREDEFINED_ROLES) {
                if (!roleRepository.existsByName(roleName)) {
                    Role role = new Role(roleName);
                    roleRepository.save(role);
                    System.out.println("Created role: " + roleName);
                    roleAdded = true;
                }
            }

            // Only print if no roles were added (everything was already valid)
            if (!roleAdded) {
                System.out.println("roles - valid");
            }
        };
    }
}