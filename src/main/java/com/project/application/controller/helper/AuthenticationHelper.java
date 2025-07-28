package com.project.application.controller.helper;

import com.project.application.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * Helper class for authentication and session management
 */
@Component
public class AuthenticationHelper {

    private static final String LOGGED_IN_USER = "loggedInUser";

    /**
     * Get the logged-in user from session
     */
    public User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute(LOGGED_IN_USER);
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn(HttpSession session) {
        return getLoggedInUser(session) != null;
    }

    /**
     * Set user session attributes after login
     */
    public void setUserSession(HttpSession session, User user) {
        session.setAttribute(LOGGED_IN_USER, user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userEmail", user.getEmailAddress());
        session.setAttribute("userFirstName", user.getFirstName());
        session.setAttribute("userLastName", user.getLastName());
        session.setAttribute("userPhone", user.getPhoneNumber());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("userRoleName", user.getRoleName());
    }

    /**
     * Set responsibility information for managers
     * UPDATED: Now stores both name and ID
     */
    public void setResponsibilitySession(HttpSession session, String responsibilityName, Long responsibilityId) {
        session.setAttribute("userResponsibilityName", responsibilityName);
        session.setAttribute("userResponsibilityId", responsibilityId);
    }

    /**
     * Clear responsibility information from session
     */
    public void clearResponsibilitySession(HttpSession session) {
        session.removeAttribute("userResponsibilityName");
        session.removeAttribute("userResponsibilityId");
    }

    /**
     * Update user session after profile changes
     */
    public void updateUserSession(HttpSession session, User user) {
        session.setAttribute(LOGGED_IN_USER, user);
        session.setAttribute("userFirstName", user.getFirstName());
        session.setAttribute("userLastName", user.getLastName());
        session.setAttribute("userPhone", user.getPhoneNumber());
    }

    /**
     * Clear session on logout
     */
    public void clearSession(HttpSession session) {
        session.invalidate();
    }

    /**
     * Get user role from session
     */
    public String getUserRole(HttpSession session) {
        return (String) session.getAttribute("userRoleName");
    }
}