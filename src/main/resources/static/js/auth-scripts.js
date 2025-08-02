/**
 * Authentication Pages - Combined JavaScript
 * Handles both login and register page functionality
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize shared functionality
    initPasswordToggle();
    initFormLoadingStates();
    initMobileFocus();

    // Initialize page-specific functionality
    if (document.getElementById('phoneNumber')) {
        initPhoneRestriction(); // Register page only
    }
});

/**
 * Password Toggle Functionality
 * Shows/hides password field with toggle button
 */
function initPasswordToggle() {
    const passwordInput = document.getElementById('password');
    const passwordToggle = document.getElementById('passwordToggle');

    if (passwordInput && passwordToggle) {
        passwordToggle.addEventListener('click', function() {
            const type = passwordInput.type === 'password' ? 'text' : 'password';
            passwordInput.type = type;

            // Update text
            this.textContent = type === 'password' ? 'SHOW' : 'HIDE';

            // Update aria-label for accessibility
            this.setAttribute('aria-label', type === 'password' ? 'Show password' : 'Hide password');
        });
    }
}

/**
 * Form Loading States
 * Adds loading state to submit buttons on form submission
 */
function initFormLoadingStates() {
    // Login form loading state
    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');

    if (loginForm && loginBtn) {
        loginForm.addEventListener('submit', function() {
            loginBtn.classList.add('loading');
            loginBtn.disabled = true;
            loginBtn.textContent = 'Signing in...';
        });
    }

    // Register form loading state
    const registerForm = document.getElementById('registerForm');
    const registerBtn = document.getElementById('registerBtn');

    if (registerForm && registerBtn) {
        registerForm.addEventListener('submit', function() {
            registerBtn.classList.add('loading');
            registerBtn.disabled = true;
            registerBtn.textContent = 'Creating Account...';
        });
    }
}

/**
 * Mobile Focus Handling
 * Autofocus email field on desktop only (prevents mobile keyboard issues)
 */
function initMobileFocus() {
    if (window.innerWidth > 768) {
        const emailField = document.getElementById('email') || document.getElementById('emailAddress');
        if (emailField) {
            emailField.focus();
        }
    }
}

/**
 * Phone Number Input Restriction
 * Limits phone input to 10 digits only (register page)
 */
function initPhoneRestriction() {
    const phoneInput = document.getElementById('phoneNumber');

    if (phoneInput) {
        phoneInput.addEventListener('input', function() {
            // Remove any non-digit characters and limit to 10 digits
            this.value = this.value.replace(/[^0-9]/g, '').slice(0, 10);
        });
    }
}