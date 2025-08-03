// Responsibility View Scripts

// Global variables for request and return functionality
let currentItemId = null;
let currentItemName = null;
let currentAction = null; // 'request' or 'return'

/**
 * Show request confirmation modal
 */
function showRequestModal(itemId, itemName) {
    currentItemId = itemId;
    currentItemName = itemName;
    currentAction = 'request';

    document.getElementById('requestMessage').textContent =
        `Are you sure you want to request "${itemName}"?`;
    document.getElementById('requestModal').style.display = 'block';
}

/**
 * NEW: Show return confirmation modal
 */
function showReturnModal(itemId, itemName) {
    currentItemId = itemId;
    currentItemName = itemName;
    currentAction = 'return';

    document.getElementById('returnMessage').textContent =
        `Are you sure you want to return "${itemName}"?`;
    document.getElementById('returnModal').style.display = 'block';
}

/**
 * Close request modal
 */
function closeRequestModal() {
    document.getElementById('requestModal').style.display = 'none';
    currentItemId = null;
    currentItemName = null;
    currentAction = null;
}

/**
 * NEW: Close return modal
 */
function closeReturnModal() {
    document.getElementById('returnModal').style.display = 'none';
    currentItemId = null;
    currentItemName = null;
    currentAction = null;
}

/**
 * Confirm item request
 */
function confirmItemRequest() {
    if (!currentItemId) {
        return;
    }

    const confirmBtn = document.getElementById('confirmRequestBtn');
    const originalText = confirmBtn.textContent;

    // Disable button and show loading
    confirmBtn.disabled = true;
    confirmBtn.textContent = 'Requesting...';

    // Send AJAX request
    fetch('/user/request-item', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `itemId=${currentItemId}`
    })
        .then(response => response.text())
        .then(result => {
            const [status, message] = result.split(':', 2);

            if (status === 'success') {
                // Store success message to show after page refresh
                sessionStorage.setItem('toastMessage', message);
                sessionStorage.setItem('toastType', 'success');
                closeRequestModal();
                // Refresh the page to update button states
                window.location.reload();
            } else {
                // Show error immediately (no page refresh needed)
                if (typeof showToast !== 'undefined') {
                    showToast(message, 'error');
                } else {
                    alert(message); // Fallback
                }
                // Re-enable button
                confirmBtn.disabled = false;
                confirmBtn.textContent = originalText;
            }
        })
        .catch(error => {
            console.error('Error:', error);
            const errorMessage = 'An error occurred while processing your request';

            // Show error immediately (no page refresh needed)
            if (typeof showToast !== 'undefined') {
                showToast(errorMessage, 'error');
            } else {
                alert(errorMessage); // Fallback
            }

            // Re-enable button
            confirmBtn.disabled = false;
            confirmBtn.textContent = originalText;
        });
}

/**
 * NEW: Confirm item return
 */
function confirmItemReturn() {
    if (!currentItemId) {
        return;
    }

    const confirmBtn = document.getElementById('confirmReturnBtn');
    const originalText = confirmBtn.textContent;

    // Disable button and show loading
    confirmBtn.disabled = true;
    confirmBtn.textContent = 'Processing...';

    // Send AJAX request
    fetch('/user/return-item', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `itemId=${currentItemId}`
    })
        .then(response => response.text())
        .then(result => {
            const [status, message] = result.split(':', 2);

            if (status === 'success') {
                // Store success message to show after page refresh
                sessionStorage.setItem('toastMessage', message);
                sessionStorage.setItem('toastType', 'success');
                closeReturnModal();
                // Refresh the page to update button states
                window.location.reload();
            } else {
                // Show error immediately (no page refresh needed)
                if (typeof showToast !== 'undefined') {
                    showToast(message, 'error');
                } else {
                    alert(message); // Fallback
                }
                // Re-enable button
                confirmBtn.disabled = false;
                confirmBtn.textContent = originalText;
            }
        })
        .catch(error => {
            console.error('Error:', error);
            const errorMessage = 'An error occurred while processing your return';

            // Show error immediately (no page refresh needed)
            if (typeof showToast !== 'undefined') {
                showToast(errorMessage, 'error');
            } else {
                alert(errorMessage); // Fallback
            }

            // Re-enable button
            confirmBtn.disabled = false;
            confirmBtn.textContent = originalText;
        });
}

/**
 * Close modal when clicking outside
 */
window.onclick = function(event) {
    const requestModal = document.getElementById('requestModal');
    const returnModal = document.getElementById('returnModal');

    if (event.target === requestModal) {
        closeRequestModal();
    } else if (event.target === returnModal) {
        closeReturnModal();
    }
}

/**
 * Close modal with Escape key
 */
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeRequestModal();
        closeReturnModal();
    }
});

/**
 * Initialize page functionality
 */
document.addEventListener('DOMContentLoaded', function() {
    // Check for toast messages from session storage (after page refresh)
    const toastMessage = sessionStorage.getItem('toastMessage');
    const toastType = sessionStorage.getItem('toastType');

    if (toastMessage && toastType) {
        // Clear the stored message
        sessionStorage.removeItem('toastMessage');
        sessionStorage.removeItem('toastType');

        // Show the toast after a small delay to ensure everything is loaded
        setTimeout(() => {
            if (typeof showToast !== 'undefined') {
                showToast(toastMessage, toastType);
            }
        }, 300);
    }
});