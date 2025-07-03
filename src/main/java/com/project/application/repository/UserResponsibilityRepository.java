package com.project.application.repository;

import com.project.application.entity.UserResponsibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserResponsibilityRepository extends JpaRepository<UserResponsibility, Long> {

    // Find user's current responsibility
    @Query("SELECT ur FROM UserResponsibility ur WHERE ur.user.userId = :userId")
    Optional<UserResponsibility> findByUserId(@Param("userId") Long userId);

    // Find all managers for a specific responsibility
    @Query("SELECT ur FROM UserResponsibility ur WHERE ur.responsibility.responsibilityId = :responsibilityId")
    List<UserResponsibility> findByResponsibilityId(@Param("responsibilityId") Long responsibilityId);

    // Check if user already has a responsibility
    boolean existsByUser_UserId(Long userId);

    // Count managers for a responsibility
    @Query("SELECT COUNT(ur) FROM UserResponsibility ur WHERE ur.responsibility.responsibilityId = :responsibilityId")
    long countByResponsibilityId(@Param("responsibilityId") Long responsibilityId);

    // Delete user assignment
    void deleteByUser_UserId(Long userId);

    // Delete all assignments for a responsibility
    void deleteByResponsibility_ResponsibilityId(Long responsibilityId);
}