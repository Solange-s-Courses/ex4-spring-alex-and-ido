package com.project.application.repository;

import com.project.application.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Find role by name (for getting specific roles like "user", "admin", etc.)
    Optional<Role> findByName(String name);
    // Check if role exists by name
    boolean existsByName(String name);
}