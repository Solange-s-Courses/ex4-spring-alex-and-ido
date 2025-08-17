/**
 * Password Toggle Functionality
 * Shows/hides password text for both login and register forms
 */
function initPasswordToggle() {
    const passwordToggle = document.getElementById('passwordToggle');
    const passwordInput = document.getElementById('password');

    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            this.textContent = type === 'password' ? 'SHOW' : 'HIDE';
            this.setAttribute('aria-label', type === 'password' ? 'Show password' : 'Hide password');
        });
    }
}

/**
 * Authentication Pages - JavaScript
 * Handles login and register page functionality
 * ENHANCED: Added comprehensive password validation
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize functionality
    initFormLoadingStates();
    initMobileFocus();

    // Initialize page-specific functionality
    if (document.getElementById('phoneNumber')) {
        initPhoneRestriction(); // Register page only
        initPasswordValidation(); // Register page only
        initPasswordToggle(); // Register page only
    }

    // Initialize password toggle for login page too
    if (document.getElementById('loginForm')) {
        initPasswordToggle(); // Login page
    }
});

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
        registerForm.addEventListener('submit', function(e) {
            // Validate password before submitting
            if (!validatePasswordRequirements()) {
                e.preventDefault();
                return false;
            }

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
        const emailField = document.getElementById('email') || document.getElementById('emailAddress') || document.getElementById('username');
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

/**
 * PASSWORD VALIDATION SYSTEM
 * Comprehensive real-time password validation with visual feedback
 */
function initPasswordValidation() {
    const passwordInput = document.getElementById('password');
    const passwordContainer = passwordInput.closest('.form-group');

    if (!passwordInput) return;

    // Create validation feedback container
    createPasswordValidationFeedback(passwordContainer);

    // Add real-time validation
    passwordInput.addEventListener('input', function() {
        validatePasswordRealTime(this.value);
    });

    // Add focus/blur handlers
    passwordInput.addEventListener('focus', function() {
        showPasswordValidation();
    });

    passwordInput.addEventListener('blur', function() {
        // Only hide if password is valid or empty
        if (this.value === '' || validatePasswordRequirements()) {
            hidePasswordValidation();
        }
    });
}

/**
 * Create password validation feedback UI
 */
function createPasswordValidationFeedback(container) {
    const existingFeedback = container.querySelector('.password-validation-feedback');
    if (existingFeedback) return;

    const validationHTML = `
        <div class="password-validation-feedback" id="passwordValidation">
            <div class="validation-requirement" data-rule="length">
                <span class="req-icon">✗</span>
                <span>8-16 characters long</span>
            </div>
            <div class="validation-requirement" data-rule="letter">
                <span class="req-icon">✗</span>
                <span>At least one letter (a-z, A-Z)</span>
            </div>
            <div class="validation-requirement" data-rule="number">
                <span class="req-icon">✗</span>
                <span>At least one number (0-9)</span>
            </div>
            <div class="validation-requirement" data-rule="special">
                <span class="req-icon">✗</span>
                <span>At least one special character (@$!%*?&)</span>
            </div>
        </div>
    `;

    // Insert after the password field container
    const passwordFieldContainer = container.querySelector('.password-field-container') || container.querySelector('.form-input');
    passwordFieldContainer.insertAdjacentHTML('afterend', validationHTML);
}

/**
 * Real-time password validation
 */
function validatePasswordRealTime(password) {
    const validationContainer = document.getElementById('passwordValidation');
    if (!validationContainer) return;

    const rules = {
        length: password.length >= 8 && password.length <= 16,
        letter: /[a-zA-Z]/.test(password),
        number: /[0-9]/.test(password),
        special: /[@$!%*?&]/.test(password)
    };

    // Update each validation requirement
    Object.keys(rules).forEach(rule => {
        const element = validationContainer.querySelector(`[data-rule="${rule}"]`);
        const icon = element.querySelector('.req-icon');

        if (rules[rule]) {
            element.classList.remove('failed');
            element.classList.add('passed');
            icon.textContent = '✓';
        } else {
            element.classList.remove('passed');
            element.classList.add('failed');
            icon.textContent = '✗';
        }
    });

    // Update password input styling
    const passwordInput = document.getElementById('password');
    const allPassed = Object.values(rules).every(passed => passed);

    if (password.length === 0) {
        // Empty password - neutral state
        passwordInput.classList.remove('error', 'valid');
    } else if (allPassed) {
        // Valid password
        passwordInput.classList.remove('error');
        passwordInput.classList.add('valid');
    } else {
        // Invalid password
        passwordInput.classList.remove('valid');
        passwordInput.classList.add('error');
    }

    return allPassed;
}

/**
 * Show password validation feedback
 */
function showPasswordValidation() {
    const validationContainer = document.getElementById('passwordValidation');
    if (validationContainer) {
        validationContainer.style.display = 'block';
        // Trigger validation for current password
        const passwordInput = document.getElementById('password');
        if (passwordInput.value) {
            validatePasswordRealTime(passwordInput.value);
        }
    }
}

/**
 * Hide password validation feedback
 */
function hidePasswordValidation() {
    const validationContainer = document.getElementById('passwordValidation');
    if (validationContainer) {
        validationContainer.style.display = 'none';
    }
}

/**
 * Validate password requirements (for form submission)
 */
function validatePasswordRequirements() {
    const passwordInput = document.getElementById('password');
    if (!passwordInput) return true; // Not on register page

    const password = passwordInput.value;

    // Check all requirements
    const isValid = password.length >= 8 &&
        password.length <= 16 &&
        /[a-zA-Z]/.test(password) &&
        /[0-9]/.test(password) &&
        /[@$!%*?&]/.test(password);

    if (!isValid) {
        // Show validation feedback and scroll to password field
        showPasswordValidation();
        validatePasswordRealTime(password);
        passwordInput.focus();

        // Show error message
        showPasswordError('Please ensure your password meets all requirements.');
        return false;
    }

    // Clear any error messages
    clearPasswordError();
    return true;
}

/**
 * Show password error message
 */
function showPasswordError(message) {
    const passwordContainer = document.getElementById('password').closest('.form-group');
    let errorElement = passwordContainer.querySelector('.password-error');

    if (!errorElement) {
        errorElement = document.createElement('div');
        errorElement.className = 'field-error password-error';

        const validationFeedback = passwordContainer.querySelector('.password-validation-feedback');
        if (validationFeedback) {
            validationFeedback.insertAdjacentElement('afterend', errorElement);
        } else {
            passwordContainer.appendChild(errorElement);
        }
    }

    errorElement.textContent = message;
    errorElement.style.display = 'block';
}

/**
 * Clear password error message
 */
function clearPasswordError() {
    const errorElement = document.querySelector('.password-error');
    if (errorElement) {
        errorElement.style.display = 'none';
    }
}