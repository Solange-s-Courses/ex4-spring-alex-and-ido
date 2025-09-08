/**
 * Authentication Pages - JavaScript
 * Clean and minimal - only essential functionality
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize core functionality
    initFormLoadingStates();
    initMobileFocus();
    initPasswordToggle();

    // Initialize page-specific functionality
    if (document.getElementById('phoneNumber')) {
        // Register page only
        initPhoneRestriction();
        initPasswordValidation();
    }
});

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
 * Password Validation System
 * Always visible validation feedback on register page
 */
function initPasswordValidation() {
    const passwordInput = document.getElementById('password');
    const passwordContainer = passwordInput.closest('.form-group');

    if (!passwordInput) return;

    // Ensure validation feedback exists and is always visible
    ensurePasswordValidationExists(passwordContainer);

    // Add real-time validation
    passwordInput.addEventListener('input', function() {
        validatePasswordRealTime(this.value);
    });
}

/**
 * Ensure password validation feedback exists
 */
function ensurePasswordValidationExists(container) {
    let validationContainer = document.getElementById('passwordValidation');

    if (!validationContainer) {
        const validationHTML = `
            <div class="password-validation-feedback" id="passwordValidation" style="display: block !important;">
                <div class="validation-requirement" data-rule="length">
                    <span class="req-icon">✗</span>
                    <span>8-16 characters</span>
                </div>
                <div class="validation-requirement" data-rule="letter">
                    <span class="req-icon">✗</span>
                    <span>One letter (a-z, A-Z)</span>
                </div>
                <div class="validation-requirement" data-rule="number">
                    <span class="req-icon">✗</span>
                    <span>One number (0-9)</span>
                </div>
                <div class="validation-requirement" data-rule="special">
                    <span class="req-icon">✗</span>
                    <span>One special (@$!%*?&)</span>
                </div>
            </div>
        `;

        const passwordFieldContainer = container.querySelector('.password-field-container') || container.querySelector('.form-input');
        passwordFieldContainer.insertAdjacentHTML('afterend', validationHTML);

        validationContainer = document.getElementById('passwordValidation');
    }

    // Ensure it's always visible on register page
    validationContainer.style.display = 'block';
}

/**
 * Real-time password validation - only updates validation feedback
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

    const allPassed = Object.values(rules).every(passed => passed);
    return allPassed;
}

/**
 * Validate password requirements (for form submission)
 */
function validatePasswordRequirements() {
    const passwordInput = document.getElementById('password');
    if (!passwordInput) return true;

    const password = passwordInput.value;
    const isValid = password.length >= 8 &&
        password.length <= 16 &&
        /[a-zA-Z]/.test(password) &&
        /[0-9]/.test(password) &&
        /[@$!%*?&]/.test(password);

    if (!isValid) {
        validatePasswordRealTime(password);
        passwordInput.focus();
        showPasswordError('Please ensure your password meets all requirements.');
        return false;
    }

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