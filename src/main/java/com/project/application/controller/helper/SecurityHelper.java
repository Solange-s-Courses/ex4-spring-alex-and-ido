package com.project.application.controller.helper;

import com.project.application.entity.User;
import com.project.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * STEP 4: New helper class for Spring Security authentication
 * Replaces the old session-based AuthenticationHelper
 */
@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final UserService userService;

    /**
     * Get the currently authenticated user from Spring Security context
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.findByEmailAddress(auth.getName()).orElse(null);
        }
        return null;
    }

    /**
     * Check if user is currently authenticated
     */
    public boolean isUserAuthenticated() {
        return getCurrentUser() != null;
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String roleName) {
        User user = getCurrentUser();
        return user != null && roleName.equals(user.getRoleName());
    }

    /**
     * Check if user has admin role
     */
    public boolean isAdmin() {
        return hasRole("admin");
    }

    /**
     * Check if user has chief role
     */
    public boolean isChief() {
        return hasRole("chief");
    }

    /**
     * Check if user has manager role
     */
    public boolean isManager() {
        return hasRole("manager");
    }

    /**
     * Check if user has regular user role
     */
    public boolean isRegularUser() {
        return hasRole("user");
    }

    /**
     * Check if user has admin or chief privileges
     */
    public boolean isAdminOrChief() {
        return isAdmin() || isChief();
    }
}