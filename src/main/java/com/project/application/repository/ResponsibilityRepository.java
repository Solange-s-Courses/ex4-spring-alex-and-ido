package com.project.application.repository;

import com.project.application.entity.Responsibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResponsibilityRepository extends JpaRepository<Responsibility, Long> {

    // Find responsibility by name
    Optional<Responsibility> findByResponsibilityName(String responsibilityName);

    // Check if responsibility exists by name
    boolean existsByResponsibilityName(String responsibilityName);
}