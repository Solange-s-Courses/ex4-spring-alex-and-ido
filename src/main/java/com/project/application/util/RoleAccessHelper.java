package com.project.application.util;

import jakarta.servlet.http.HttpSession;

public class RoleAccessHelper {

    // Check if user is logged in
    public static boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("loggedInUser") != null;
    }

    // Get user role name from session
    public static String getUserRole(HttpSession session) {
        return (String) session.getAttribute("userRoleName");
    }

    // Check if user has specific role
    public static boolean hasRole(HttpSession session, String roleName) {
        String userRole = getUserRole(session);
        return userRole != null && userRole.equals(roleName);
    }

    // Check if user is admin
    public static boolean isAdmin(HttpSession session) {
        return hasRole(session, "admin");
    }

    // Check if user is chief
    public static boolean isChief(HttpSession session) {
        return hasRole(session, "chief");
    }

    // Check if user is manager
    public static boolean isManager(HttpSession session) {
        return hasRole(session, "manager");
    }

    // Check if user is regular user
    public static boolean isUser(HttpSession session) {
        return hasRole(session, "user");
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
            case "admin" -> "/admin-dashboard";
            case "chief" -> "/chief-dashboard";
            case "manager" -> "/manager-dashboard";
            default -> "/dashboard";
        };
    }
}