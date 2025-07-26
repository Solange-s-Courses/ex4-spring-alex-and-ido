package com.project.application.controller.helper;

import com.project.application.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class for role-based access control
 */
@Component
public class AccessControlHelper {

    @Autowired
    private AuthenticationHelper authHelper;

    /**
     * Check if user has admin role
     */
    public boolean isAdmin(HttpSession session) {
        User user = authHelper.getLoggedInUser(session);
        return user != null && "admin".equals(user.getRoleName());
    }

    /**
     * Check if user has chief role
     */
    public boolean isChief(HttpSession session) {
        User user = authHelper.getLoggedInUser(session);
        return user != null && "chief".equals(user.getRoleName());
    }

    /**
     * Check if user has manager role
     */
    public boolean isManager(HttpSession session) {
        User user = authHelper.getLoggedInUser(session);
        return user != null && "manager".equals(user.getRoleName());
    }

    /**
     * Check if user has user role
     */
    public boolean isRegularUser(HttpSession session) {
        User user = authHelper.getLoggedInUser(session);
        return user != null && "user".equals(user.getRoleName());
    }

    /**
     * Check if user has admin or chief privileges
     */
    public boolean isAdminOrChief(HttpSession session) {
        return isAdmin(session) || isChief(session);
    }

    /**
     * Get redirect path for unauthorized access
     */
    public String getUnauthorizedRedirect(HttpSession session) {
        if (!authHelper.isUserLoggedIn(session)) {
            return "redirect:/login";
        }
        return "error/404";
    }

    /**
     * Validate user access and return appropriate redirect if denied
     * @param session HTTP session
     * @param requiredRole Required role ("admin", "chief", "manager", "user")
     * @return null if access granted, redirect string if denied
     */
    public String validateAccess(HttpSession session, String requiredRole) {
        if (!authHelper.isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = authHelper.getLoggedInUser(session);
        if (!requiredRole.equals(user.getRoleName())) {
            return "error/404";
        }

        return null; // Access granted
    }

    /**
     * Validate multiple roles access
     */
    public String validateMultipleRoles(HttpSession session, String... allowedRoles) {
        if (!authHelper.isUserLoggedIn(session)) {
            return "redirect:/login";
        }

        User user = authHelper.getLoggedInUser(session);
        String userRole = user.getRoleName();

        for (String allowedRole : allowedRoles) {
            if (allowedRole.equals(userRole)) {
                return null; // Access granted
            }
        }

        return "error/404"; // Access denied
    }
}