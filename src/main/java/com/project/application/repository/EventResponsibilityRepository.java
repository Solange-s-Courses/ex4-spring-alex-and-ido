package com.project.application.repository;

import com.project.application.entity.EventResponsibility;
import com.project.application.entity.Responsibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventResponsibilityRepository extends JpaRepository<EventResponsibility, Long> {

    /**
     * Find all responsibilities assigned to a specific event
     */
    @Query("SELECT er.responsibility FROM EventResponsibility er WHERE er.event.eventId = :eventId")
    List<Responsibility> findResponsibilitiesByEventId(@Param("eventId") Long eventId);

    /**
     * Check if a specific responsibility is already assigned to a specific event
     */
    boolean existsByEventEventIdAndResponsibilityResponsibilityId(Long eventId, Long responsibilityId);

    /**
     * Find specific assignment between event and responsibility
     */
    Optional<EventResponsibility> findByEventEventIdAndResponsibilityResponsibilityId(Long eventId, Long responsibilityId);

    /**
     * Delete specific assignment between event and responsibility
     */
    void deleteByEventEventIdAndResponsibilityResponsibilityId(Long eventId, Long responsibilityId);

    /**
     * Find all responsibilities NOT assigned to a specific event
     */
    @Query("SELECT r FROM Responsibility r WHERE r.responsibilityId NOT IN " +
            "(SELECT er.responsibility.responsibilityId FROM EventResponsibility er WHERE er.event.eventId = :eventId)")
    List<Responsibility> findResponsibilitiesNotAssignedToEvent(@Param("eventId") Long eventId);

    /**
     * Delete responsibility by the responsibility ID
     */
    void deleteByResponsibility_ResponsibilityId(Long responsibilityId);

    /**
     * Find all event assignments for a specific responsibility
     */
    @Query("SELECT er FROM EventResponsibility er WHERE er.responsibility.responsibilityId = :responsibilityId")
    List<EventResponsibility> findByResponsibilityId(@Param("responsibilityId") Long responsibilityId);

    /**
     * Find all responsibility assignments for a specific event
     */
    @Query("SELECT er FROM EventResponsibility er WHERE er.event.eventId = :eventId")
    List<EventResponsibility> findByEventId(@Param("eventId") Long eventId);

    /**
     * Delete all event-responsibility assignments for a specific event
     * Used when deleting an event to remove all its responsibility assignments
     */
    void deleteByEventEventId(Long eventId);
}