package com.project.application.util;

import jakarta.servlet.http.HttpSession;

public class RoleAccessHelper {

    public static final String ADMIN = "admin";
    public static final String CHIEF = "chief";
    public static final String MANAGER = "manager";
    public static final String USER = "user";
    private static final String LOGGED_IN_USER = "loggedInUser";
    private static final String USER_ROLE_NAME = "userRoleName";

    // Check if user is logged in
    public static boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(LOGGED_IN_USER) != null;
    }

    // Get user role name from session
    public static String getUserRole(HttpSession session) {
        return (String) session.getAttribute(USER_ROLE_NAME);
    }

    // Check if user has specific role
    public static boolean hasRole(HttpSession session, String roleName) {
        String userRole = getUserRole(session);
        return userRole != null && userRole.equals(roleName);
    }

    // Check if user is admin
    public static boolean isAdmin(HttpSession session) {
        return hasRole(session, ADMIN);
    }

    // Check if user is chief
    public static boolean isChief(HttpSession session) {
        return hasRole(session, CHIEF);
    }

    // Check if user is manager
    public static boolean isManager(HttpSession session) {
        return hasRole(session, MANAGER);
    }

    // Check if user is regular user
    public static boolean isUser(HttpSession session) {
        return hasRole(session, USER);
    }

    // Check if user has admin or chief privileges
    public static boolean canManageEvents(HttpSession session) {
        return isAdmin(session) || isChief(session);
    }

    // Check if user can manage responsibilities (admin, chief, or manager)
    public static boolean canManageResponsibilities(HttpSession session) {
        return isAdmin(session) || isChief(session) || isManager(session);
    }

    // Redirect URL based on role (for future use)
    public static String getDefaultPageForRole(String roleName) {
        return switch (roleName) {
            case ADMIN -> "/admin-dashboard";
            case CHIEF -> "/chief-dashboard";
            case MANAGER -> "/manager-dashboard";
            default -> "/dashboard";
        };
    }
}