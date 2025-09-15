package com.project.application.service;

import com.project.application.entity.*;
import com.project.application.repository.UserRepository;
import com.project.application.repository.UserResponsibilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Spring Security imports
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

/**
 * Service layer for comprehensive user management in the logistics system.

 * Handles the complete user lifecycle including:
 * - User registration and authentication (with Spring Security integration)
 * - Role-based access control and role transitions
 * - Responsibility assignment for managers
 * - Admin user management (promote/demote chiefs, user deletion)
 * - Profile updates and password management with BCrypt encryption
 * - Complex cascade deletion with foreign key constraint handling

 * Business Rules:
 * - Users start with "user" role by default
 * - Managers are assigned specific responsibilities
 * - Chiefs can be promoted from any non-admin role
 * - Admin users are protected from modification/deletion
 * - Cascade deletion maintains database integrity

 * Security Features:
 * - BCrypt password encryption with migration support
 * - Spring Security UserDetailsService implementation
 * - Role-based authorization support
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    // Validation patterns
    private static final String NAME_PATTERN = "^[A-Za-z]{1,20}$";
    private static final String PHONE_PATTERN = "^[0-9]{10}$";

    // Role constants
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_CHIEF = "chief";
    private static final String ROLE_MANAGER = "manager";
    private static final String ROLE_USER = "user";

    // BCrypt identifier
    private static final String BCRYPT_PREFIX = "$2a$";

    // Repository Dependencies
    private final UserRepository userRepository;
    private final UserResponsibilityRepository userResponsibilityRepository;

    // Service Dependencies
    private final ItemService itemService;
    private final RoleService roleService;
    private final ResponsibilityService responsibilityService;
    private final RequestService requestService;

    // Security Dependencies
    private final PasswordEncoder passwordEncoder;

    // ========== SPRING SECURITY INTEGRATION ==========

    /**
     * Loads user details for Spring Security authentication.
     * Converts database user to Spring Security UserDetails format.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAddress(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmailAddress())
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRoleName().toUpperCase())))
                .build();
    }

    // ========== USER REGISTRATION & AUTHENTICATION ==========

    /**
     * Registers a new user with default "user" role and encrypted password.
     * Validates uniqueness of email and phone number.
     *
     * @param user User object with registration details
     * @return "success" if registration successful, error message otherwise
     */
    public String registerUser(User user) {
        try {
            // Normalize user input
            normalizeUserInput(user);

            // Validate uniqueness
            String uniquenessValidation = validateUserUniqueness(user);
            if (!uniquenessValidation.equals("success")) {
                return uniquenessValidation;
            }

            // Set up new user
            setupNewUser(user);

            userRepository.save(user);
            return "success";

        } catch (DataIntegrityViolationException e) {
            return "Database constraint violation - duplicate data detected";
        } catch (Exception e) {
            return "Registration failed!";
        }
    }

    /**
     * Authenticates user login with BCrypt support and legacy password migration.
     * Automatically migrates plain text passwords to BCrypt on successful login.
     *
     * @param email User's email address
     * @param password User's plain text password
     * @return User object if authentication successful, null otherwise
     */
    public User loginUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmailAddress(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String storedPassword = user.getPassword();

            if (isBCryptEncoded(storedPassword)) {
                // Use BCrypt verification for encrypted passwords
                if (passwordEncoder.matches(password, storedPassword)) {
                    return user;
                }
            } else {
                // Legacy plain text password - verify and migrate to BCrypt
                if (password.equals(storedPassword)) {
                    migratePasswordToBCrypt(user, password);
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Verifies admin password with BCrypt support and legacy compatibility.
     */
    public boolean verifyAdminPassword(Long adminId, String password) {
        try {
            Optional<User> adminOptional = userRepository.findById(adminId);
            if (adminOptional.isPresent()) {
                User admin = adminOptional.get();
                String storedPassword = admin.getPassword();

                if (isBCryptEncoded(storedPassword)) {
                    return passwordEncoder.matches(password, storedPassword);
                } else {
                    return password.equals(storedPassword);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== USER PROFILE MANAGEMENT ==========

    /**
     * Updates user's first and last name with validation.
     */
    public String updateUserName(User loggedInUser, String firstName, String lastName) {
        try {
            // Validate and normalize names
            String validationResult = validateAndNormalizeNames(firstName, lastName);
            if (!validationResult.equals("success")) {
                return validationResult;
            }

            // Update user
            loggedInUser.setFirstName(firstName.trim().toLowerCase());
            loggedInUser.setLastName(lastName.trim().toLowerCase());
            userRepository.save(loggedInUser);
            return "success";

        } catch (Exception e) {
            return "Failed to change name. Please try again.";
        }
    }

    /**
     * Updates user's phone number with validation and uniqueness check.
     */
    public String updateUserPhone(User loggedInUser, String phoneNumber) {
        try {
            phoneNumber = phoneNumber.trim();

            // Validate phone format
            if (!phoneNumber.matches(PHONE_PATTERN)) {
                return "Phone number must be exactly 10 digits.";
            }

            // Check uniqueness (excluding current user)
            if (isPhoneNumberTaken(phoneNumber, loggedInUser.getUserId())) {
                return "Phone number already exists!";
            }

            loggedInUser.setPhoneNumber(phoneNumber);
            userRepository.save(loggedInUser);
            return "success";

        } catch (Exception e) {
            return "Failed to change phone number. Please try again.";
        }
    }

    // ========== ROLE MANAGEMENT ==========

    /**
     * Changes user role (generic method for admin functionality).
     */
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

    /**
     * Promotes any non-admin user to chief role.
     * Automatically removes manager responsibilities if applicable.
     */
    @Transactional
    public String promoteToChief(Long userId) {
        try {
            User user = getUserOrFail(userId);
            if (user == null) return "User not found";

            // Validation checks
            if (ROLE_ADMIN.equals(user.getRoleName())) {
                return "Cannot modify admin users";
            }
            if (ROLE_CHIEF.equals(user.getRoleName())) {
                return "User is already a chief";
            }

            // Remove manager responsibilities before promotion
            if (ROLE_MANAGER.equals(user.getRoleName())) {
                String removeResult = removeUserFromResponsibility(userId);
                if (!removeResult.equals("success")) {
                    return "Failed to remove user from responsibility: " + removeResult;
                }
            }

            // Promote to chief
            return assignRoleToUser(user, ROLE_CHIEF);

        } catch (Exception e) {
            return "Failed to promote user to chief: " + e.getMessage();
        }
    }

    /**
     * Demotes chief to regular user role.
     */
    @Transactional
    public String demoteChief(Long userId) {
        try {
            User user = getUserOrFail(userId);
            if (user == null) return "User not found";

            // Validation checks
            if (ROLE_ADMIN.equals(user.getRoleName())) {
                return "Cannot modify admin users";
            }
            if (!ROLE_CHIEF.equals(user.getRoleName())) {
                return "User is not a chief";
            }

            // Demote to regular user
            return assignRoleToUser(user, ROLE_USER);

        } catch (Exception e) {
            return "Failed to demote chief: " + e.getMessage();
        }
    }

    /**
     * Checks if the specified user is the last chief in the system.
     */
    public boolean isLastChief(Long userId) {
        try {
            long chiefCount = getAllNonAdminUsers().stream()
                    .filter(u -> ROLE_CHIEF.equals(u.getRoleName()))
                    .count();

            if (chiefCount == 1) {
                Optional<User> userOptional = userRepository.findById(userId);
                return userOptional.isPresent() && ROLE_CHIEF.equals(userOptional.get().getRoleName());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== RESPONSIBILITY MANAGEMENT ==========

    /**
     * Assigns a responsibility to a user and promotes them to manager role.
     */
    @Transactional
    public String assignResponsibility(Long userId, String responsibilityName) {
        try {
            // Validate input
            if (responsibilityName == null || responsibilityName.trim().isEmpty()) {
                return "Responsibility name cannot be empty";
            }

            User user = getUserOrFail(userId);
            if (user == null) return "User not found";

            // Check if user already has responsibility
            if (userResponsibilityRepository.existsByUser_UserId(userId)) {
                return "User already has a responsibility assigned";
            }

            // Get or create responsibility
            Responsibility responsibility = getOrCreateResponsibility(responsibilityName.trim());

            // Create assignment and promote user
            createResponsibilityAssignment(user, responsibility);
            assignRoleToUser(user, ROLE_MANAGER);

            return "success";

        } catch (Exception e) {
            return "Failed to assign responsibility: " + e.getMessage();
        }
    }

    /**
     * Removes user from their responsibility and demotes to user role if needed.
     */
    @Transactional
    public String removeUserFromResponsibility(Long userId) {
        try {
            Optional<UserResponsibility> userResponsibilityOptional =
                    userResponsibilityRepository.findByUserId(userId);

            if (userResponsibilityOptional.isEmpty()) {
                return "User has no responsibility assigned";
            }

            Long responsibilityId = userResponsibilityOptional.get()
                    .getResponsibility().getResponsibilityId();

            // Remove user assignment
            userResponsibilityRepository.deleteByUser_UserId(userId);

            // Handle responsibility cleanup if no managers remain
            handleResponsibilityCleanup(responsibilityId);

            // Demote manager to user (if applicable)
            demoteManagerToUser(userId);

            return "success";

        } catch (Exception e) {
            return "Failed to remove responsibility: " + e.getMessage();
        }
    }

    // ========== USER DELETION & ADMIN OPERATIONS ==========

    /**
     * Deletes a user with complete cascade cleanup of all related data.
     * Handles foreign key constraints by deleting in proper order:
     * 1. User's owned items → set to "Unavailable"
     * 2. User's requests → deleted
     * 3. Responsibility assignments → removed/cleaned up
     * 4. User → deleted
     */
    @Transactional
    public String deleteUser(Long userId) {
        try {
            User userToDelete = getUserOrFail(userId);
            if (userToDelete == null) return "User not found";

            // Safety check: prevent deleting admin users
            if (ROLE_ADMIN.equals(userToDelete.getRole().getName())) {
                return "Cannot delete admin users";
            }

            // Step 1: Handle user's owned items
            handleUserItemsOnDeletion(userId);

            // Step 2: Handle responsibility assignments
            handleUserResponsibilityOnDeletion(userId);

            // Step 3: Clean up user's requests
            requestService.deleteRequestsByUserId(userId);

            // Step 4: Delete the user
            userRepository.deleteById(userId);
            return "success";

        } catch (Exception e) {
            return "Failed to delete user: " + e.getMessage();
        }
    }

    /**
     * Updates user information by admin (names and role).
     */
    public String updateUserByAdmin(Long userId, String firstName, String lastName, String roleName) {
        try {
            User user = getUserOrFail(userId);
            if (user == null) return "User not found";

            // Update names if provided
            if (firstName != null && lastName != null) {
                String nameValidation = validateAndUpdateUserNames(user, firstName, lastName);
                if (!nameValidation.equals("success")) {
                    return nameValidation;
                }
            }

            // Update role if provided
            if (roleName != null && !roleName.trim().isEmpty()) {
                String roleValidation = validateAndUpdateUserRole(user, roleName);
                if (!roleValidation.equals("success")) {
                    return roleValidation;
                }
            }

            userRepository.save(user);
            return "success";

        } catch (Exception e) {
            return "Failed to update user: " + e.getMessage();
        }
    }

    /**
     * Deletes all non-admin users (critical admin functionality).
     */
    @Transactional
    public String deleteAllNonAdminUsers() {
        try {
            List<User> nonAdminUsers = userRepository.findAllNonAdminUsers();

            if (nonAdminUsers.isEmpty()) {
                return "No users to delete";
            }

            int deletedCount = nonAdminUsers.size();

            // Clean up all responsibility assignments first
            cleanupAllResponsibilityAssignments(nonAdminUsers);

            // Delete all users
            nonAdminUsers.forEach(userRepository::delete);

            return "success:" + deletedCount;

        } catch (Exception e) {
            return "Failed to delete users: " + e.getMessage();
        }
    }

    // ========== QUERY METHODS ==========

    /**
     * Gets all non-admin users for management purposes.
     */
    public List<User> getAllNonAdminUsers() {
        return userRepository.findAllNonAdminUsers();
    }

    /**
     * Gets all users for admin user management interface.
     */
    public List<User> getAllUsersForManagement() {
        return userRepository.findAllNonAdminUsers();
    }

    /**
     * Gets managers and users with responsibility information populated.
     */
    public List<User> getManagersAndUsersWithResponsibilities() {
        List<User> users = getAllNonAdminUsers().stream()
                .filter(u -> ROLE_MANAGER.equals(u.getRole().getName()) ||
                        ROLE_USER.equals(u.getRole().getName()))
                .collect(Collectors.toList());

        // Populate responsibility names
        users.forEach(user -> {
            String responsibilityName = getUserResponsibilityName(user.getUserId());
            user.setResponsibilityName(responsibilityName);
        });

        return users;
    }

    /**
     * Gets all managers for a specific responsibility.
     */
    public List<User> getResponsibilityManagers(Long responsibilityId) {
        return userResponsibilityRepository.findByResponsibilityId(responsibilityId)
                .stream()
                .map(UserResponsibility::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Gets user's current responsibility name.
     */
    public String getUserResponsibilityName(Long userId) {
        return userResponsibilityRepository.findByUserId(userId)
                .map(ur -> ur.getResponsibility().getResponsibilityName())
                .orElse(null);
    }

    /**
     * Gets user's current responsibility ID.
     */
    public Long getUserResponsibilityId(Long userId) {
        return userResponsibilityRepository.findByUserId(userId)
                .map(ur -> ur.getResponsibility().getResponsibilityId())
                .orElse(null);
    }

    // ========== FINDER METHODS ==========

    public Optional<User> findByEmailAddress(String emailAddress) {
        return userRepository.findByEmailAddress(emailAddress);
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Normalizes user input by trimming and converting to lowercase where appropriate.
     */
    private void normalizeUserInput(User user) {
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
    }

    /**
     * Validates that email and phone are unique in the system.
     */
    private String validateUserUniqueness(User user) {
        if (userRepository.findByEmailAddress(user.getEmailAddress()).isPresent()) {
            return "Email address already exists!";
        }
        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            return "Phone number already exists!";
        }
        return "success";
    }

    /**
     * Sets up a new user with default role, timestamp, and encrypted password.
     */
    private void setupNewUser(User user) {
        Role defaultRole = roleService.getDefaultUserRole();
        user.setRole(defaultRole);
        user.setDateOfIssue(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    /**
     * Validates and normalizes first and last names.
     */
    private String validateAndNormalizeNames(String firstName, String lastName) {
        firstName = firstName.trim().toLowerCase();
        lastName = lastName.trim().toLowerCase();

        if (!firstName.matches(NAME_PATTERN)) {
            return "First name must contain only letters and be 1-20 characters long.";
        }
        if (!lastName.matches(NAME_PATTERN)) {
            return "Last name must contain only letters and be 1-20 characters long.";
        }
        return "success";
    }

    /**
     * Checks if a phone number is already taken by another user.
     */
    private boolean isPhoneNumberTaken(String phoneNumber, Long excludeUserId) {
        Optional<User> existingUser = userRepository.findByPhoneNumber(phoneNumber);
        return existingUser.isPresent() && !existingUser.get().getUserId().equals(excludeUserId);
    }

    /**
     * Checks if password is BCrypt encoded.
     */
    private boolean isBCryptEncoded(String password) {
        return password.startsWith(BCRYPT_PREFIX);
    }

    /**
     * Migrates legacy plain text password to BCrypt.
     */
    private void migratePasswordToBCrypt(User user, String plainTextPassword) {
        user.setPassword(passwordEncoder.encode(plainTextPassword));
        userRepository.save(user);
    }

    /**
     * Helper to get user by ID with null handling.
     */
    private User getUserOrFail(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Assigns a role to a user with error handling.
     */
    private String assignRoleToUser(User user, String roleName) {
        Optional<Role> roleOptional = roleService.findByName(roleName);
        if (roleOptional.isEmpty()) {
            return roleName + " role not found in system";
        }

        user.setRole(roleOptional.get());
        userRepository.save(user);
        return "success";
    }

    /**
     * Gets or creates a responsibility by name.
     */
    private Responsibility getOrCreateResponsibility(String responsibilityName) {
        return responsibilityService.findByName(responsibilityName)
                .orElseGet(() -> responsibilityService.createResponsibility(responsibilityName));
    }

    /**
     * Creates user-responsibility assignment.
     */
    private void createResponsibilityAssignment(User user, Responsibility responsibility) {
        UserResponsibility userResponsibility = new UserResponsibility(user, responsibility);
        userResponsibilityRepository.save(userResponsibility);
    }

    /**
     * Handles responsibility cleanup when no managers remain.
     */
    private void handleResponsibilityCleanup(Long responsibilityId) {
        long remainingManagers = userResponsibilityRepository.countByResponsibilityId(responsibilityId);
        if (remainingManagers == 0) {
            itemService.deleteAllItemsByResponsibilityId(responsibilityId);
            responsibilityService.deleteResponsibility(responsibilityId);
        }
    }

    /**
     * Demotes manager to user role if appropriate.
     */
    private void demoteManagerToUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (ROLE_MANAGER.equals(user.getRoleName())) {
                assignRoleToUser(user, ROLE_USER);
            }
        }
    }

    /**
     * Handles user's owned items when user is deleted.
     */
    private void handleUserItemsOnDeletion(Long userId) {
        List<Item> userItems = itemService.getItemsByUserId(userId);
        userItems.forEach(item -> {
            item.setUser(null);
            item.setStatus("Unavailable");
            itemService.saveItem(item);
        });
    }

    /**
     * Handles user's responsibility assignments when user is deleted.
     */
    private void handleUserResponsibilityOnDeletion(Long userId) {
        Optional<UserResponsibility> userResponsibility =
                userResponsibilityRepository.findByUserId(userId);

        if (userResponsibility.isPresent()) {
            Long responsibilityId = userResponsibility.get().getResponsibility().getResponsibilityId();

            userResponsibilityRepository.deleteByUser_UserId(userId);

            long remainingManagers = userResponsibilityRepository.countByResponsibilityId(responsibilityId);
            if (remainingManagers == 0) {
                // Delete requests first to avoid foreign key constraints
                requestService.deleteRequestsByResponsibilityId(responsibilityId);
                itemService.deleteAllItemsByResponsibilityId(responsibilityId);
                responsibilityService.deleteResponsibility(responsibilityId);
            }
        }
    }

    /**
     * Validates and updates usernames for admin operations.
     */
    private String validateAndUpdateUserNames(User user, String firstName, String lastName) {
        String validation = validateAndNormalizeNames(firstName, lastName);
        if (!validation.equals("success")) {
            return validation;
        }

        user.setFirstName(firstName.trim().toLowerCase());
        user.setLastName(lastName.trim().toLowerCase());
        return "success";
    }

    /**
     * Validates and updates user role for admin operations.
     */
    private String validateAndUpdateUserRole(User user, String roleName) {
        Optional<Role> roleOptional = roleService.findByName(roleName);
        if (roleOptional.isEmpty()) {
            return "Role not found";
        }

        if (ROLE_ADMIN.equals(user.getRole().getName())) {
            return "Cannot modify admin users";
        }
        if (ROLE_ADMIN.equals(roleName)) {
            return "Cannot assign admin role";
        }

        user.setRole(roleOptional.get());
        return "success";
    }

    /**
     * Cleans up all responsibility assignments for multiple users.
     */
    private void cleanupAllResponsibilityAssignments(List<User> users) {
        for (User user : users) {
            Optional<UserResponsibility> userResponsibility =
                    userResponsibilityRepository.findByUserId(user.getUserId());

            if (userResponsibility.isPresent()) {
                Long responsibilityId = userResponsibility.get().getResponsibility().getResponsibilityId();
                userResponsibilityRepository.deleteByUser_UserId(user.getUserId());

                long remainingManagers = userResponsibilityRepository.countByResponsibilityId(responsibilityId);
                if (remainingManagers == 0) {
                    // CRITICAL: Delete requests and items first to avoid foreign key constraint
                    requestService.deleteRequestsByResponsibilityId(responsibilityId);
                    itemService.deleteAllItemsByResponsibilityId(responsibilityId);
                    responsibilityService.deleteResponsibility(responsibilityId);
                }
            }
        }
    }

    @Transactional
    public String demoteAllManagers() {
        try {
            List<User> managers = getAllNonAdminUsers().stream()
                    .filter(u -> ROLE_MANAGER.equals(u.getRoleName()))
                    .collect(Collectors.toList());

            if (managers.isEmpty()) {
                return "No managers to demote";
            }

            int demotedCount = 0;

            for (User manager : managers) {
                // Remove from responsibility (this handles cascade deletion)
                String removeResult = removeUserFromResponsibility(manager.getUserId());
                if (removeResult.equals("success") || removeResult.equals("User has no responsibility assigned")) {
                    // Demote to user role
                    assignRoleToUser(manager, ROLE_USER);
                    demotedCount++;
                }
            }

            return "success:" + demotedCount;

        } catch (Exception e) {
            return "Failed to demote managers: " + e.getMessage();
        }
    }

    @Transactional
    public String demoteAllChiefs() {
        try {
            List<User> chiefs = getAllNonAdminUsers().stream()
                    .filter(u -> ROLE_CHIEF.equals(u.getRoleName()))
                    .collect(Collectors.toList());

            if (chiefs.isEmpty()) {
                return "No chiefs to demote";
            }

            int demotedCount = 0;

            for (User chief : chiefs) {
                // Demote to user role (chiefs don't have responsibilities to remove)
                String demoteResult = assignRoleToUser(chief, ROLE_USER);
                if ("success".equals(demoteResult)) {
                    demotedCount++;
                }
            }

            return "success:" + demotedCount;

        } catch (Exception e) {
            return "Failed to demote chiefs: " + e.getMessage();
        }
    }
}