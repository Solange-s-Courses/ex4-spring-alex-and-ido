package com.project.application.repository;

import com.project.application.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Find all items for a specific responsibility
    @Query("SELECT i FROM Item i WHERE i.responsibility.responsibilityId = :responsibilityId ORDER BY i.itemName")
    List<Item> findByResponsibilityId(@Param("responsibilityId") Long responsibilityId);

    // Find item by name within a specific responsibility (for duplicate checking)
    @Query("SELECT i FROM Item i WHERE i.itemName = :itemName AND i.responsibility.responsibilityId = :responsibilityId")
    Optional<Item> findByItemNameAndResponsibilityId(@Param("itemName") String itemName, @Param("responsibilityId") Long responsibilityId);

    // Check if item name exists within a responsibility
    boolean existsByItemNameAndResponsibility_ResponsibilityId(String itemName, Long responsibilityId);

    // Count items in a specific responsibility
    @Query("SELECT COUNT(i) FROM Item i WHERE i.responsibility.responsibilityId = :responsibilityId")
    long countByResponsibilityId(@Param("responsibilityId") Long responsibilityId);

    // Delete all items for a responsibility (used when responsibility is deleted)
    void deleteByResponsibility_ResponsibilityId(Long responsibilityId);

    // Find items by status within a responsibility
    @Query("SELECT i FROM Item i WHERE i.responsibility.responsibilityId = :responsibilityId AND i.status = :status ORDER BY i.itemName")
    List<Item> findByResponsibilityIdAndStatus(@Param("responsibilityId") Long responsibilityId, @Param("status") String status);

    // Find all items owned by a specific user
    @Query("SELECT i FROM Item i WHERE i.user.userId = :userId ORDER BY i.itemName")
    List<Item> findByUser_UserId(@Param("userId") Long userId);

    // Count items owned by a specific user
    @Query("SELECT COUNT(i) FROM Item i WHERE i.user.userId = :userId")
    long countByUser_UserId(@Param("userId") Long userId);
}