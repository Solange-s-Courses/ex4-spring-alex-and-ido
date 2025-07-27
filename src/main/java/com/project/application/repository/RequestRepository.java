package com.project.application.repository;

import com.project.application.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // Find all requests for a specific responsibility (for manager to see)
    @Query("SELECT r FROM Request r WHERE r.item.responsibility.responsibilityId = :responsibilityId ORDER BY r.dateOfIssue DESC")
    List<Request> findByResponsibilityId(@Param("responsibilityId") Long responsibilityId);

    // Find all requests by a specific user
    @Query("SELECT r FROM Request r WHERE r.user.userId = :userId ORDER BY r.dateOfIssue DESC")
    List<Request> findByUserId(@Param("userId") Long userId);

    // Find all requests for a specific item
    @Query("SELECT r FROM Request r WHERE r.item.itemId = :itemId ORDER BY r.dateOfIssue DESC")
    List<Request> findByItemId(@Param("itemId") Long itemId);

    // Check if user already has pending request for specific item
    @Query("SELECT r FROM Request r WHERE r.user.userId = :userId AND r.item.itemId = :itemId")
    Optional<Request> findByUserIdAndItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);

    // Check if user has pending request for specific item (boolean version)
    boolean existsByUser_UserIdAndItem_ItemId(Long userId, Long itemId);

    // Find requests by type (request/return)
    @Query("SELECT r FROM Request r WHERE r.requestType = :requestType ORDER BY r.dateOfIssue DESC")
    List<Request> findByRequestType(@Param("requestType") String requestType);

    // Find requests by responsibility and type
    @Query("SELECT r FROM Request r WHERE r.item.responsibility.responsibilityId = :responsibilityId AND r.requestType = :requestType ORDER BY r.dateOfIssue DESC")
    List<Request> findByResponsibilityIdAndRequestType(@Param("responsibilityId") Long responsibilityId, @Param("requestType") String requestType);

    // Count pending requests for a responsibility
    @Query("SELECT COUNT(r) FROM Request r WHERE r.item.responsibility.responsibilityId = :responsibilityId")
    long countByResponsibilityId(@Param("responsibilityId") Long responsibilityId);

    // Count pending requests by user
    @Query("SELECT COUNT(r) FROM Request r WHERE r.user.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    // Delete all requests for a specific item (when item is deleted)
    void deleteByItem_ItemId(Long itemId);

    // Delete all requests by a specific user (when user is deleted)
    void deleteByUser_UserId(Long userId);

    // Delete all requests for items in a responsibility (when responsibility is deleted)
    @Query("DELETE FROM Request r WHERE r.item.responsibility.responsibilityId = :responsibilityId")
    void deleteByResponsibilityId(@Param("responsibilityId") Long responsibilityId);
}