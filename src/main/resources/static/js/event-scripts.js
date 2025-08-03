// Event Management JavaScript
// This file handles all event-related functionality including lifecycle management,
// responsibility assignment, and modal interactions

// Global variables for modal management
let currentEventId = null;
let isProcessing = false;
let responsibilityToRemove = null;

// These will be set when the script loads via data attributes
let eventId = null;
let userRole = null;

// Initialize script with data from HTML
function initializeEventScript() {
    const scriptTag = document.querySelector('script[data-event-id]');
    if (scriptTag) {
        eventId = scriptTag.getAttribute('data-event-id');
        userRole = scriptTag.getAttribute('data-user-role');
    }
}

// Fast-click protection function
function preventFastClick(button, callback, duration = 1000) {
    if (button.disabled || isProcessing) return;

    button.disabled = true;
    isProcessing = true;

    callback();

    setTimeout(() => {
        button.disabled = false;
        isProcessing = false;
    }, duration);
}

// ======================
// RESPONSIBILITY MANAGEMENT
// ======================

// Show Add Responsibility Modal
function showAddResponsibilityModal() {
    const modal = document.getElementById('addResponsibilityModal');

    // Prevent opening if already open or loading
    if (modal.classList.contains('show') || modal.classList.contains('loading')) {
        return;
    }

    // Set loading state
    modal.classList.add('loading');

    fetch(`/chief/events/${eventId}/available-responsibilities`)
        .then(response => response.json())
        .then(data => {
            if (!data || data.length === 0) {
                showToast('No responsibilities available to add. All responsibilities are already assigned to this event.', 'info');
                return;
            }

            const select = document.getElementById('responsibilitySelect');
            select.innerHTML = '<option value="">Choose a responsibility...</option>';

            data.forEach(responsibility => {
                const option = document.createElement('option');
                option.value = responsibility.id;
                option.textContent = responsibility.name;
                select.appendChild(option);
            });

            // Show modal only after successful load
            modal.classList.add('show');
        })
        .catch(error => {
            console.error('Error loading responsibilities:', error);
            showToast('Error loading available responsibilities', 'error');
        })
        .finally(() => {
            // Always remove loading state
            modal.classList.remove('loading');
        });
}

// Hide Add Responsibility Modal
function hideAddResponsibilityModal() {
    const modal = document.getElementById('addResponsibilityModal');
    modal.classList.remove('show');
    modal.classList.remove('loading');
}

// Add Selected Responsibility
function addSelectedResponsibility() {
    const select = document.getElementById('responsibilitySelect');
    const responsibilityId = select.value;
    const modal = document.getElementById('addResponsibilityModal');

    if (!responsibilityId) {
        showToast('Please select a responsibility', 'error');
        return;
    }

    // Prevent multiple submissions
    if (modal.classList.contains('submitting')) {
        return;
    }

    modal.classList.add('submitting');

    const formData = new FormData();
    formData.append('responsibilityId', responsibilityId);

    fetch(`/chief/events/${eventId}/add-responsibility`, {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updateResponsibilitiesList(data.responsibilities);
                hideAddResponsibilityModal();
            } else {
                showToast(data.message || 'Error adding responsibility', 'error');
            }
        })
        .catch(error => {
            console.error('Error adding responsibility:', error);
            showToast('Error adding responsibility', 'error');
        })
        .finally(() => {
            modal.classList.remove('submitting');
        });
}

// Show Remove Responsibility Modal
function removeResponsibility(responsibilityId) {
    // Find responsibility name from current list
    const responsibilityItem = document.querySelector(`[onclick*="removeResponsibility(${responsibilityId})"]`).closest('.responsibility-item');
    const responsibilityName = responsibilityItem.querySelector('.responsibility-name').textContent;

    // Store for confirmation
    responsibilityToRemove = { id: responsibilityId, name: responsibilityName };

    // Update modal content
    document.getElementById('responsibilityNameToRemove').textContent = responsibilityName;

    // Show modal
    document.getElementById('removeResponsibilityModal').classList.add('show');
}

// Hide Remove Responsibility Modal
function hideRemoveResponsibilityModal() {
    document.getElementById('removeResponsibilityModal').classList.remove('show');
    responsibilityToRemove = null;
}

// Confirm Remove Responsibility
function confirmRemoveResponsibility() {
    if (!responsibilityToRemove) {
        return;
    }

    const formData = new FormData();
    formData.append('responsibilityId', responsibilityToRemove.id);

    fetch(`/chief/events/${eventId}/remove-responsibility`, {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updateResponsibilitiesList(data.responsibilities);
                hideRemoveResponsibilityModal();
                showToast('Responsibility removed successfully!', 'success');
            } else {
                showToast(data.message || 'Error removing responsibility', 'error');
            }
        })
        .catch(error => {
            console.error('Error removing responsibility:', error);
            showToast('Error removing responsibility', 'error');
        });
}

// ======================
// EVENT LIFECYCLE MANAGEMENT
// ======================

// Activate Event Modal
function showActivateEventModal(button) {
    if (isProcessing) return;

    const eventIdFromButton = button.getAttribute('data-event-id');
    const eventName = button.getAttribute('data-event-name');

    // Check if event has responsibilities before showing modal
    const responsibilitiesContainer = document.getElementById('responsibilitiesContainer');
    const emptyState = document.getElementById('emptyState');

    // If empty state exists, it means no responsibilities
    if (emptyState || !responsibilitiesContainer.querySelector('.responsibility-item')) {
        showToast('Event must have at least one responsibility before activation', 'error');
        return;
    }

    currentEventId = eventIdFromButton;

    document.getElementById('activateEventName').textContent = eventName;
    document.getElementById('activateEventModal').classList.add('show');
}

function closeActivateEventModal() {
    document.getElementById('activateEventModal').classList.remove('show');
    currentEventId = null;
}

function confirmActivateEvent() {
    if (!currentEventId || isProcessing) return;

    const confirmBtn = document.getElementById('confirmActivateBtn');
    preventFastClick(confirmBtn, () => {
        const originalText = confirmBtn.textContent;
        confirmBtn.textContent = 'Activating...';

        fetch(`/chief/events/${currentEventId}/activate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showToast('Event activated successfully!', 'success');
                    closeActivateEventModal();
                    // Reload page to update UI
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                } else {
                    showToast(data.message || 'Failed to activate event', 'error');
                    confirmBtn.textContent = originalText;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('An error occurred while activating the event', 'error');
                confirmBtn.textContent = originalText;
            });
    }, 2000);
}

// Return Mode Modal
function showReturnModeModal(button) {
    if (isProcessing) return;

    const eventIdFromButton = button.getAttribute('data-event-id');
    const eventName = button.getAttribute('data-event-name');

    currentEventId = eventIdFromButton;

    document.getElementById('returnModeEventName').textContent = eventName;
    document.getElementById('returnModeModal').classList.add('show');
}

function closeReturnModeModal() {
    document.getElementById('returnModeModal').classList.remove('show');
    currentEventId = null;
}

function confirmReturnMode() {
    if (!currentEventId || isProcessing) return;

    const confirmBtn = document.getElementById('confirmReturnModeBtn');
    preventFastClick(confirmBtn, () => {
        const originalText = confirmBtn.textContent;
        confirmBtn.textContent = 'Switching...';

        fetch(`/chief/events/${currentEventId}/switch-to-return`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // No success toast - silent update
                    closeReturnModeModal();
                    // Reload page to update UI
                    setTimeout(() => {
                        window.location.reload();
                    }, 500);
                } else {
                    showToast(data.message || 'Failed to switch to return mode', 'error');
                    confirmBtn.textContent = originalText;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('An error occurred while switching to return mode', 'error');
                confirmBtn.textContent = originalText;
            });
    }, 2000);
}

// Active Mode Modal
function showActiveModeModal(button) {
    if (isProcessing) return;

    const eventIdFromButton = button.getAttribute('data-event-id');
    const eventName = button.getAttribute('data-event-name');

    currentEventId = eventIdFromButton;

    document.getElementById('activeModeEventName').textContent = eventName;
    document.getElementById('activeModeModal').classList.add('show');
}

function closeActiveModeModal() {
    document.getElementById('activeModeModal').classList.remove('show');
    currentEventId = null;
}

function confirmActiveMode() {
    if (!currentEventId || isProcessing) return;

    const confirmBtn = document.getElementById('confirmActiveModeBtn');
    preventFastClick(confirmBtn, () => {
        const originalText = confirmBtn.textContent;
        confirmBtn.textContent = 'Switching...';

        fetch(`/chief/events/${currentEventId}/switch-to-active`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // No success toast - silent update
                    closeActiveModeModal();
                    // Reload page to update UI
                    setTimeout(() => {
                        window.location.reload();
                    }, 500);
                } else {
                    showToast(data.message || 'Failed to switch to active mode', 'error');
                    confirmBtn.textContent = originalText;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('An error occurred while switching to active mode', 'error');
                confirmBtn.textContent = originalText;
            });
    }, 2000);
}

// Complete Event Modal
function showCompleteEventModal(button) {
    if (isProcessing) return;

    const eventIdFromButton = button.getAttribute('data-event-id');
    const eventName = button.getAttribute('data-event-name');

    currentEventId = eventIdFromButton;

    document.getElementById('completeEventName').textContent = eventName;
    document.getElementById('completeEventModal').classList.add('show');
}

function closeCompleteEventModal() {
    document.getElementById('completeEventModal').classList.remove('show');
    currentEventId = null;
}

function confirmCompleteEvent() {
    if (!currentEventId || isProcessing) return;

    const confirmBtn = document.getElementById('confirmCompleteBtn');
    preventFastClick(confirmBtn, () => {
        const originalText = confirmBtn.textContent;
        confirmBtn.textContent = 'Completing...';

        fetch(`/chief/events/${currentEventId}/complete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // No success toast - silent update
                    closeCompleteEventModal();
                    // Reload page to update UI
                    setTimeout(() => {
                        window.location.reload();
                    }, 500);
                } else {
                    showToast(data.message || 'Failed to complete event', 'error');
                    confirmBtn.textContent = originalText;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showToast('An error occurred while completing the event', 'error');
                confirmBtn.textContent = originalText;
            });
    }, 2000);
}

// ======================
// EDIT EVENT MODAL
// ======================

// Edit Event Modal Functions
function showEditEventModalFromData(button) {
    const eventName = button.getAttribute('data-event-name');
    const eventDescription = button.getAttribute('data-event-description');
    showEditEventModal(eventName, eventDescription);
}

function showEditEventModal(eventName, description) {
    // Pre-fill the form with current values
    document.getElementById('editEventName').value = eventName || '';
    document.getElementById('editDescription').value = description || '';

    // Update character counter for pre-filled content
    updateCharacterCount();

    // Hide any previous error messages
    document.getElementById('editEventError').style.display = 'none';

    // Show the modal
    document.getElementById('editEventModal').classList.add('show');
}

function hideEditEventModal() {
    document.getElementById('editEventModal').classList.remove('show');

    // Clear form
    document.getElementById('editEventName').value = '';
    document.getElementById('editDescription').value = '';

    // Hide error message
    document.getElementById('editEventError').style.display = 'none';
}

function saveEventChanges() {
    const eventName = document.getElementById('editEventName').value.trim();
    const description = document.getElementById('editDescription').value.trim();

    // Basic validation
    if (!eventName) {
        showEditError('Event name is required');
        return;
    }

    if (eventName.length > 100) {
        showEditError('Event name cannot exceed 100 characters');
        return;
    }

    if (description.length > 200) {
        showEditError('Description cannot exceed 200 characters');
        return;
    }

    // Submit the form
    const formData = new FormData();
    formData.append('eventName', eventName);
    formData.append('description', description || '');

    fetch(`/chief/events/${eventId}/edit`, {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('Event updated successfully!', 'success');
                hideEditEventModal();
                // Reload the page to show updated information
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            } else {
                showEditError(data.message || 'Error updating event');
            }
        })
        .catch(error => {
            console.error('Error updating event:', error);
            showEditError('Error updating event');
        });
}

function showEditError(message) {
    const errorDiv = document.getElementById('editEventError');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
}

function updateCharacterCount() {
    const textarea = document.getElementById('editDescription');
    const counter = document.getElementById('editDescriptionCount');
    if (textarea && counter) {
        const currentLength = textarea.value.length;
        counter.textContent = `${currentLength}/200 characters`;

        // Change color when approaching limit
        if (currentLength > 180) {
            counter.style.color = '#dc3545';
        } else if (currentLength > 160) {
            counter.style.color = '#ffc107';
        } else {
            counter.style.color = '#6c757d';
        }
    }
}

// ======================
// UI UPDATE FUNCTIONS
// ======================

// Update Responsibilities List
function updateResponsibilitiesList(responsibilities) {
    const container = document.getElementById('responsibilitiesContainer');

    if (!responsibilities || responsibilities.length === 0) {
        container.innerHTML = '<div class="no-responsibilities" id="emptyState">No responsibilities assigned yet</div>';
        return;
    }

    let html = '';
    responsibilities.forEach(resp => {
        // Only show remove button for chiefs and not-active events
        const removeButton = (userRole === 'chief') ?
            `<button class="remove-responsibility-btn" onclick="removeResponsibility(${resp.id})">×</button>` : '';

        html += `
            <div class="responsibility-item">
                ${removeButton}
                <div class="responsibility-name">${resp.name}</div>
                <div class="responsibility-description ${!resp.description ? 'no-description' : ''}">
                    ${resp.description || 'No description'}
                </div>
                <div class="responsibility-managers">
                    ${resp.managers && resp.managers.length > 0 ? (function() {
            let managerHtml = '';
            // Show first 2 managers
            for (let i = 0; i < Math.min(2, resp.managers.length); i++) {
                managerHtml += `<span class="manager-badge">${resp.managers[i]}</span>`;
            }
            // Add counter if more than 2 managers
            if (resp.managers.length > 2) {
                managerHtml += `<span class="manager-badge manager-counter">+${resp.managers.length - 2} more</span>`;
            }
            return managerHtml;
        })() : '<div class="no-managers">No managers assigned</div>'
        }
                </div>
                <div class="responsibility-actions">
                    <a href="/responsibility/view/${resp.id}" class="view-responsibility-btn">View Details</a>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

// ======================
// INITIALIZATION
// ======================

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize script with data from HTML
    initializeEventScript();

    // Initialize character counter
    const editDescTextarea = document.getElementById('editDescription');
    if (editDescTextarea) {
        editDescTextarea.addEventListener('input', updateCharacterCount);
    }
});