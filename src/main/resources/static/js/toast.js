/**
 * Toast Notification System
 * A reusable toast notification system for the entire application
 */

// Global variable to store the auto-hide timeout
let toastTimeout = null;

/**
 * Display a toast notification
 * @param {string} message - The message to display
 * @param {string} type - The type of toast (success, error, info, warning)
 * @param {number} duration - How long to display the toast in milliseconds (default: 6000)
 */
function showToast(message, type = 'success', duration = 6000) {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');

    if (toast && toastMessage) {
        // Clear any existing timeout
        if (toastTimeout) {
            clearTimeout(toastTimeout);
        }

        toastMessage.textContent = message;
        toast.className = `toast toast-${type} show`;

        // Auto-hide after specified duration
        if (duration > 0) {
            toastTimeout = setTimeout(() => {
                hideToast();
            }, duration);
        }
    } else {
        // Fallback to console if toast elements don't exist
        console.warn('Toast elements not found. Message:', message);
    }
}

/**
 * Manually hide the toast notification
 */
function hideToast() {
    const toast = document.getElementById('toast');
    if (toast) {
        toast.className = 'toast';

        // Clear the timeout if it exists
        if (toastTimeout) {
            clearTimeout(toastTimeout);
            toastTimeout = null;
        }
    }
}

/**
 * Convenience methods for different toast types
 */
let Toast = {
    success: (message, duration = 6000) => showToast(message, 'success', duration),
    error: (message, duration = 6000) => showToast(message, 'error', duration),
    info: (message, duration = 6000) => showToast(message, 'info', duration),
    warning: (message, duration = 6000) => showToast(message, 'warning', duration),

    // Persistent toasts (don't auto-hide)
    persistent: {
        success: (message) => showToast(message, 'success', 0),
        error: (message) => showToast(message, 'error', 0),
        info: (message) => showToast(message, 'info', 0),
        warning: (message) => showToast(message, 'warning', 0)
    },

    hide: hideToast
};

// Initialize toast functionality when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Close toast when pressing Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            hideToast();
        }
    });

    // Add click event to close button if it exists
    const closeButton = document.getElementById('toastClose');
    if (closeButton) {
        closeButton.addEventListener('click', hideToast);
    }
});

// Make Toast available globally
window.Toast = Toast;