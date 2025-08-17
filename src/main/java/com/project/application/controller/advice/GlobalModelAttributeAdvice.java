package com.project.application.controller.advice;

import com.project.application.controller.helper.SecurityHelper;
import com.project.application.entity.User;
import com.project.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice to add common model attributes to ALL pages
 * This ensures navbar has necessary data across the entire application
 *
 * Automatically provides:
 * - user: Current authenticated user
 * - userRole: User's role name
 * - userResponsibilityId: Manager's responsibility ID (for navbar button)
 * - userResponsibilityName: Manager's responsibility name
 * - isAuthenticated: Authentication status
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private final SecurityHelper securityHelper;
    private final UserService userService;

    /**
     * Add current user to ALL page models
     * Available in templates as: ${user}
     */
    @ModelAttribute("user")
    public User addUserToModel() {
        return securityHelper.getCurrentUser();
    }

    /**
     * Add user role to ALL page models
     * Available in templates as: ${userRole}
     */
    @ModelAttribute("userRole")
    public String addUserRoleToModel() {
        User user = securityHelper.getCurrentUser();
        return user != null ? user.getRoleName() : null;
    }

    /**
     * Add responsibility ID for managers (needed for navbar "Manage Responsibility" button)
     * Available in templates as: ${userResponsibilityId}
     */
    @ModelAttribute("userResponsibilityId")
    public Long addUserResponsibilityIdToModel() {
        User user = securityHelper.getCurrentUser();
        if (user != null && "manager".equals(user.getRoleName())) {
            return userService.getUserResponsibilityId(user.getUserId());
        }
        return null;
    }

    /**
     * Add responsibility name for managers (for display purposes)
     * Available in templates as: ${userResponsibilityName}
     */
    @ModelAttribute("userResponsibilityName")
    public String addUserResponsibilityNameToModel() {
        User user = securityHelper.getCurrentUser();
        if (user != null && "manager".equals(user.getRoleName())) {
            return userService.getUserResponsibilityName(user.getUserId());
        }
        return null;
    }

    /**
     * Check if user is authenticated (for template convenience)
     * Available in templates as: ${isAuthenticated}
     */
    @ModelAttribute("isAuthenticated")
    public boolean addAuthenticationStatusToModel() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
    }
}