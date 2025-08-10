// Dashboard Page Scripts - Modern Redesign

// Mobile Tab Management
let activeTab = 'events';

function switchTab(tabName) {
    activeTab = tabName;

    // Update tab buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

    // Update panels
    document.querySelectorAll('.events-panel, .responsibilities-panel').forEach(panel => {
        panel.classList.remove('active');
    });

    if (tabName === 'events') {
        document.querySelector('.events-panel').classList.add('active');
    } else {
        document.querySelector('.responsibilities-panel').classList.add('active');
    }
}

// Event Creation Modal Management
function showEventModal() {
    const modal = document.getElementById('eventCreationModal');
    if (modal) {
        modal.classList.add('show');
        // Focus on the event name input
        setTimeout(() => {
            document.getElementById('eventName').focus();
        }, 100);
    }
}

function hideEventModal() {
    const modal = document.getElementById('eventCreationModal');
    if (modal) {
        modal.classList.remove('show');
        // Clear form inputs
        document.getElementById('eventName').value = '';
        document.getElementById('description').value = '';

        // Clear any error messages
        const errorDiv = modal.querySelector('.event-form-error');
        if (errorDiv) {
            errorDiv.style.display = 'none';
        }
    }
}

// Delete Modal Management
let eventIdToDelete = null;

function showDeleteModalFromData(button) {
    const eventId = button.getAttribute('data-event-id');
    const eventName = button.getAttribute('data-event-name');
    showDeleteModal(eventId, eventName);
}

function showDeleteModal(eventId, eventName) {
    eventIdToDelete = eventId;
    document.getElementById('eventNameToDelete').textContent = eventName;
    document.getElementById('deleteModal').classList.add('show');
}

function hideDeleteModal() {
    document.getElementById('deleteModal').classList.remove('show');
    eventIdToDelete = null;
}

function confirmDeleteEvent() {
    if (eventIdToDelete) {
        document.getElementById('eventIdToDelete').value = eventIdToDelete;
        document.getElementById('deleteEventForm').submit();
    }
}

// Initialize event listeners when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Initialize mobile tabs if on mobile
    const isMobile = window.innerWidth <= 1024;
    if (isMobile) {
        // Set initial active tab
        switchTab('events');

        // Add tab click listeners
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                switchTab(this.getAttribute('data-tab'));
            });
        });
    }

    // Close modals when clicking outside
    const eventCreationModal = document.getElementById('eventCreationModal');
    if (eventCreationModal) {
        eventCreationModal.addEventListener('click', function(e) {
            if (e.target === this) {
                hideEventModal();
            }
        });
    }

    const deleteModal = document.getElementById('deleteModal');
    if (deleteModal) {
        deleteModal.addEventListener('click', function(e) {
            if (e.target === this) {
                hideDeleteModal();
            }
        });
    }

    // Handle ESC key to close modals
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            // Close event creation modal if open
            if (eventCreationModal && eventCreationModal.classList.contains('show')) {
                hideEventModal();
            }
            // Close delete modal if open
            if (deleteModal && deleteModal.classList.contains('show')) {
                hideDeleteModal();
            }
        }
    });

    // Auto-show event modal if there was an error
    const eventFormError = document.querySelector('.event-form-error');
    if (eventFormError && eventCreationModal) {
        showEventModal();
    }

    // Handle window resize for responsive behavior
    let resizeTimer;
    window.addEventListener('resize', function() {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(function() {
            const wasMobile = isMobile;
            const isNowMobile = window.innerWidth <= 1024;

            if (wasMobile !== isNowMobile) {
                location.reload(); // Simple reload for layout change
            }
        }, 250);
    });

    // Add smooth scroll behavior for panels
    document.querySelectorAll('.panel-content').forEach(panel => {
        panel.addEventListener('scroll', function() {
            // Add shadow to panel header when scrolled
            const header = this.previousElementSibling;
            if (this.scrollTop > 10) {
                header.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
            } else {
                header.style.boxShadow = 'none';
            }
        });
    });

    // Add card status classes for styling
    document.querySelectorAll('.event-card').forEach(card => {
        const statusElement = card.querySelector('.event-status');
        if (statusElement) {
            if (statusElement.classList.contains('status-active')) {
                card.classList.add('status-active');
            } else if (statusElement.classList.contains('status-not-active')) {
                card.classList.add('status-not-active');
            } else if (statusElement.classList.contains('status-equipment-return')) {
                card.classList.add('status-equipment-return');
            }
        }
    });
});

// Utility function to add ripple effect to buttons
function addRippleEffect(element) {
    element.addEventListener('click', function(e) {
        const ripple = document.createElement('span');
        ripple.classList.add('ripple');
        this.appendChild(ripple);

        const rect = this.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = e.clientX - rect.left - size / 2;
        const y = e.clientY - rect.top - size / 2;

        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = x + 'px';
        ripple.style.top = y + 'px';

        setTimeout(() => {
            ripple.remove();
        }, 600);
    });
}

// Add ripple effect to all buttons
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.create-event-btn, .view-btn, .event-view-btn').forEach(btn => {
        addRippleEffect(btn);
    });
});