package com.project.application.service;

import com.project.application.entity.Role;
import com.project.application.entity.User;
import com.project.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.project.application.entity.Responsibility;
import com.project.application.entity.UserResponsibility;
import com.project.application.repository.UserResponsibilityRepository;
import org.springframework.transaction.annotation.Transactional;

// STEP 2: Add Spring Security imports
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService { // STEP 2: Implement UserDetailsService

    private static final String NAME_PATTERN = "^[A-Za-z]{1,20}$";
    private static final String PHONE_PATTERN = "^[0-9]{10}$";

    // Dependency Injections
    private final UserRepository userRepository;
    private final ItemService itemService;
    private final RoleService roleService;
    private final ResponsibilityService responsibilityService;
    private final UserResponsibilityRepository userResponsibilityRepository;

    // STEP 2: Add PasswordEncoder dependency
    private final PasswordEncoder passwordEncoder;

    // STEP 2: Implement UserDetailsService method
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAddress(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmailAddress())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRoleName().toUpperCase())))
                .build();
    }

    // Register new user with default "user" role
    public String registerUser(User user) {

        // Trim and convert names to lowercase for storage
        if (user.getFirstName() != null) {
            user.setFirstName(user.getFirstName().trim().toLowerCase());
        }
        if (user.getLastName() != null) {
            user.setLastName(user.getLastName().trim().toLowerCase());
        }
        if (user.getEmailAddress() != null) {
            user.setEmailAddress(user.getEmailAddress().trim().toLowerCase());
        }
        if (user.getPhoneNumber() != null) {
            user.setPhoneNumber(user.getPhoneNumber().trim());
        }

        // Check for existing email
        if (userRepository.findByEmailAddress(user.getEmailAddress()).isPresent()) {
            return "Email address already exists!";
        }

        // Check for existing phone number
        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            return "Phone number already exists!";
        }

        try {
            // Get default "user" role
            Role defaultRole = roleService.getDefaultUserRole();

            // Set role and timestamp
            user.setRole(defaultRole);
            user.setDateOfIssue(LocalDateTime.now());

            // STEP 2: Encode password for new users
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            userRepository.save(user);
            return "success";

        } catch (DataIntegrityViolationException e) {
            return "Database constraint violation - duplicate data detected";
        } catch (Exception e) {
            return "Registration failed!";
        }
    }

    // STEP 2: Enhanced login method with password migration
    public User loginUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmailAddress(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String storedPassword = user.getPassword();

            // Check if password is already BCrypt encoded (starts with $2a$)
            if (storedPassword.startsWith("$2a$")) {
                // Use BCrypt verification
                if (passwordEncoder.matches(password, storedPassword)) {
                    return user;
                }
            } else {
                // Legacy plain text password - verify and migrate
                if (password.equals(storedPassword)) {
                    // Migrate to BCrypt
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user);
                    return user;
                }
            }
        }
        return null;
    }

    // Find user by email
    public Optional<User> findByEmailAddress(String emailAddress) {
        return userRepository.findByEmailAddress(emailAddress);
    }

    // Find user by phone
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    // Update username - matches your current logic
    public String updateUserName(User loggedInUser, String firstName, String lastName) {
        try {
            // Trim and convert names to lowercase
            firstName = firstName.trim().toLowerCase();
            lastName = lastName.trim().toLowerCase();

            // Validate names (only letters, max 20 characters)
            if (!firstName.matches(NAME_PATTERN)) {
                return "First name must contain only letters and be 1-20 characters long.";
            }

            if (!lastName.matches(NAME_PATTERN)) {
                return "Last name must contain only letters and be 1-20 characters long.";
            }

            // Update user in database
            loggedInUser.setFirstName(firstName);
            loggedInUser.setLastName(lastName);
            userRepository.save(loggedInUser);
            return "success";

        } catch (Exception e) {
            return "Failed to change name. Please try again.";
        }
    }

    // Update user phone - matches your current logic
    public String updateUserPhone(User loggedInUser, String phoneNumber) {
        try {
            // Trim phone number
            phoneNumber = phoneNumber.trim();

            // Validate phone number (exactly 10 digits)
            if (!phoneNumber.matches(PHONE_PATTERN)) {
                return "Phone number must be exactly 10 digits.";
            }

            // Check if phone number already exists (excluding current user)
            Optional<User> existingUser = userRepository.findByPhoneNumber(phoneNumber);
            if (existingUser.isPresent() && !existingUser.get().getUserId().equals(loggedInUser.getUserId())) {
                return "Phone number already exists!";
            }

            // Update user in database
            loggedInUser.setPhoneNumber(phoneNumber);
            userRepository.save(loggedInUser);
            return "success";

        } catch (Exception e) {
            return "Failed to change phone number. Please try again.";
        }
    }

    // NEW: Method to change user role (for future admin functionality)
    public String changeUserRole(Long userId, String roleName) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            Optional<Role> roleOptional = roleService.findByName(roleName);

            if (userOptional.isPresent() && roleOptional.isPresent()) {
                User user = userOptional.get();
                user.setRole(roleOptional.get());
                userRepository.save(user);
                return "success";
            }
            return "User or role not found";
        } catch (Exception e) {
            return "Role update failed: " + e.getMessage();
        }
    }

    public List<User> getAllNonAdminUsers() {
        return userRepository.findAllNonAdminUsers();
    }

    //Delete user by ID (an admin functionality)
    @Transactional
    public String deleteUser(Long userId) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isPresent()) {
                User userToDelete = userOptional.get();

                // Safety check: prevent deleting admin users
                if ("admin".equals(userToDelete.getRole().getName())) {
                    return "Cannot delete admin users";
                }

                // Step 1: Handle responsibility assignment if user is a manager
                Optional<UserResponsibility> userResponsibility =
                        userResponsibilityRepository.findByUserId(userId);

                if (userResponsibility.isPresent()) {
                    Long responsibilityId = userResponsibility.get().getResponsibility().getResponsibilityId();

                    // Remove user's responsibility assignment
                    userResponsibilityRepository.deleteByUser_UserId(userId);

                    // Check if responsibility has any remaining managers
                    long remainingManagers = userResponsibilityRepository.countByResponsibilityId(responsibilityId);

                    // If no managers left, delete the responsibility and its items
                    if (remainingManagers == 0) {
                        // Delete all items for this responsibility first
                        itemService.deleteAllItemsByResponsibilityId(responsibilityId);

                        // Then delete the responsibility
                        responsibilityService.deleteResponsibility(responsibilityId);
                    }
                }

                // Step 2: Now safely delete the user
                userRepository.deleteById(userId);
                return "success";

            } else {
                return "User not found";
            }
        } catch (Exception e) {
            return "Failed to delete user: " + e.getMessage();
        }
    }

    // Update user name and role (an admin functionality)
    public String updateUserByAdmin(Long userId, String firstName, String lastName, String roleName) {
        try {
            // Find the user
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                return "User not found";
            }

            User user = userOptional.get();

            // Validate and update names if provided
            if (firstName != null && lastName != null) {
                // Trim and convert names to lowercase
                firstName = firstName.trim().toLowerCase();
                lastName = lastName.trim().toLowerCase();

                // Validate names (only letters, max 20 characters)
                if (!firstName.matches(NAME_PATTERN)) {
                    return "First name must contain only letters and be 1-20 characters long.";
                }

                if (!lastName.matches(NAME_PATTERN)) {
                    return "Last name must contain only letters and be 1-20 characters long.";
                }

                user.setFirstName(firstName);
                user.setLastName(lastName);
            }

            // Update role if provided
            if (roleName != null && !roleName.trim().isEmpty()) {
                Optional<Role> roleOptional = roleService.findByName(roleName);
                if (!roleOptional.isPresent()) {
                    return "Role not found";
                }

                // Prevent changing admin role
                if ("admin".equals(user.getRole().getName())) {
                    return "Cannot modify admin users";
                }

                // Prevent setting admin role
                if ("admin".equals(roleName)) {
                    return "Cannot assign admin role";
                }

                user.setRole(roleOptional.get());
            }

            userRepository.save(user);
            return "success";

        } catch (Exception e) {
            return "Failed to update user: " + e.getMessage();
        }
    }

    // Delete all non-admin users (critical admin functionality)
    @Transactional
    public String deleteAllNonAdminUsers() {
        try {
            List<User> nonAdminUsers = userRepository.findAllNonAdminUsers();

            if (nonAdminUsers.isEmpty()) {
                return "No users to delete";
            }

            int deletedCount = nonAdminUsers.size();

            // Step 1: Remove all responsibility assignments for users being deleted
            for (User user : nonAdminUsers) {
                // Check if user has any responsibility
                Optional<UserResponsibility> userResponsibility =
                        userResponsibilityRepository.findByUserId(user.getUserId());

                if (userResponsibility.isPresent()) {
                    Long responsibilityId = userResponsibility.get().getResponsibility().getResponsibilityId();

                    // Remove user's assignment
                    userResponsibilityRepository.deleteByUser_UserId(user.getUserId());

                    // Check if responsibility has any remaining managers
                    long remainingManagers = userResponsibilityRepository.countByResponsibilityId(responsibilityId);

                    // If no managers left, delete the responsibility
                    if (remainingManagers == 0) {
                        responsibilityService.deleteResponsibility(responsibilityId);
                    }
                }
            }

            // Step 2: Now safely delete all non-admin users
            for (User user : nonAdminUsers) {
                userRepository.delete(user);
            }

            return "success:" + deletedCount;

        } catch (Exception e) {
            return "Failed to delete users: " + e.getMessage();
        }
    }

    // STEP 2: Enhanced admin password verification with BCrypt support
    public boolean verifyAdminPassword(Long adminId, String password) {
        try {
            Optional<User> adminOptional = userRepository.findById(adminId);
            if (adminOptional.isPresent()) {
                User admin = adminOptional.get();
                String storedPassword = admin.getPassword();

                // Check if password is BCrypt encoded
                if (storedPassword.startsWith("$2a$")) {
                    return passwordEncoder.matches(password, storedPassword);
                } else {
                    // Legacy plain text comparison
                    return password.equals(storedPassword);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Get user by ID
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Assign responsibility to a user and automatically make them manager
     */
    @Transactional
    public String assignResponsibility(Long userId, String responsibilityName) {
        try {
            // Validate responsibility name
            if (responsibilityName == null || responsibilityName.trim().isEmpty()) {
                return "Responsibility name cannot be empty";
            }

            responsibilityName = responsibilityName.trim();

            // Find the user
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                return "User not found";
            }

            User user = userOptional.get();

            // Check if user already has a responsibility
            if (userResponsibilityRepository.existsByUser_UserId(userId)) {
                return "User already has a responsibility assigned";
            }

            // Get or create responsibility
            Responsibility responsibility;
            Optional<Responsibility> existingResponsibility = responsibilityService.findByName(responsibilityName);

            if (existingResponsibility.isPresent()) {
                responsibility = existingResponsibility.get();
            } else {
                responsibility = responsibilityService.createResponsibility(responsibilityName);
            }

            // Create user-responsibility assignment
            UserResponsibility userResponsibility = new UserResponsibility(user, responsibility);
            userResponsibilityRepository.save(userResponsibility);

            // Change user role to manager
            Optional<Role> managerRole = roleService.findByName("manager");
            if (managerRole.isPresent()) {
                user.setRole(managerRole.get());
                userRepository.save(user);
            } else {
                return "Manager role not found in system";
            }

            return "success";

        } catch (Exception e) {
            return "Failed to assign responsibility: " + e.getMessage();
        }
    }

    /**
     * Remove user from their responsibility and demote to user role if needed
     */
    @Transactional
    public String removeUserFromResponsibility(Long userId) {
        try {
            // Find user's current responsibility assignment
            Optional<UserResponsibility> userResponsibilityOptional =
                    userResponsibilityRepository.findByUserId(userId);

            if (!userResponsibilityOptional.isPresent()) {
                return "User has no responsibility assigned";
            }

            UserResponsibility userResponsibility = userResponsibilityOptional.get();
            Long responsibilityId = userResponsibility.getResponsibility().getResponsibilityId();

            // Remove user assignment
            userResponsibilityRepository.deleteByUser_UserId(userId);

            // Check if responsibility has any remaining managers
            long remainingManagers = userResponsibilityRepository.countByResponsibilityId(responsibilityId);

            // If no managers left, delete the responsibility and its items
            if (remainingManagers == 0) {
                // Delete all items for this responsibility first
                itemService.deleteAllItemsByResponsibilityId(responsibilityId);

                // Then delete the responsibility
                responsibilityService.deleteResponsibility(responsibilityId);
            }

            // Demote user to "user" role (unless they're chief or admin)
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String currentRole = user.getRoleName();

                // Only demote if they're currently a manager
                if ("manager".equals(currentRole)) {
                    Optional<Role> userRole = roleService.findByName("user");
                    if (userRole.isPresent()) {
                        user.setRole(userRole.get());
                        userRepository.save(user);
                    }
                }
            }

            return "success";

        } catch (Exception e) {
            return "Failed to remove responsibility: " + e.getMessage();
        }
    }

    /**
     * Get user's current responsibility name (for display purposes)
     */
    public String getUserResponsibilityName(Long userId) {
        Optional<UserResponsibility> userResponsibilityOptional =
                userResponsibilityRepository.findByUserId(userId);

        if (userResponsibilityOptional.isPresent()) {
            return userResponsibilityOptional.get().getResponsibility().getResponsibilityName();
        }

        return null; // User has no responsibility
    }

    /**
     * Get all managers for a specific responsibility
     */
    public List<User> getResponsibilityManagers(Long responsibilityId) {
        List<UserResponsibility> assignments =
                userResponsibilityRepository.findByResponsibilityId(responsibilityId);

        return assignments.stream()
                .map(UserResponsibility::getUser)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get managers and users with their responsibility information populated
     */
    public List<User> getManagersAndUsersWithResponsibilities() {
        // Get only managers and users (exclude admins and chiefs)
        List<User> users = getAllNonAdminUsers().stream()
                .filter(u -> "manager".equals(u.getRole().getName()) || "user".equals(u.getRole().getName()))
                .collect(Collectors.toList());

        // Populate responsibility names for each user
        for (User user : users) {
            String responsibilityName = getUserResponsibilityName(user.getUserId());
            user.setResponsibilityName(responsibilityName);
        }

        return users;
    }

    /**
     * Get user's current responsibility ID (for URL generation)
     */
    public Long getUserResponsibilityId(Long userId) {
        Optional<UserResponsibility> userResponsibilityOptional =
                userResponsibilityRepository.findByUserId(userId);

        if (userResponsibilityOptional.isPresent()) {
            return userResponsibilityOptional.get().getResponsibility().getResponsibilityId();
        }

        return null; // User has no responsibility
    }
}