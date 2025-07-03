package com.project.application.repository;

import com.project.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Marks this as a Spring repository component (data access layer)
@Repository
public interface UserRepository extends JpaRepository<User, Long> { // User - entity type , Long - primary key type

    // findByEmailAddress => SELECT * FROM users WHERE email_address = ?
    Optional<User> findByEmailAddress(String emailAddress);

    // findByPhoneNumber => SELECT * FROM users WHERE phone_number = ?
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Get all users except admins
    @Query("SELECT u FROM User u WHERE u.role.name != 'admin' ORDER BY u.firstName, u.lastName")
    List<User> findAllNonAdminUsers();
}