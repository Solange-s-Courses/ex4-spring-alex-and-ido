// Dashboard Page Scripts

// Event Form Management
function showEventForm() {
    const formContainer = document.getElementById('eventFormContainer');
    const createBtn = document.getElementById('createEventBtn');

    formContainer.classList.add('show');
    createBtn.style.display = 'none';

    // Focus on the event name input
    document.getElementById('eventName').focus();
}

function hideEventForm() {
    const formContainer = document.getElementById('eventFormContainer');
    const createBtn = document.getElementById('createEventBtn');

    formContainer.classList.remove('show');
    createBtn.style.display = 'inline-block';

    // Clear form inputs
    document.getElementById('eventName').value = '';
    document.getElementById('description').value = '';

    // Clear any error messages
    const errorDiv = formContainer.querySelector('.event-form-error');
    if (errorDiv) {
        errorDiv.style.display = 'none';
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
    // Close modal when clicking outside
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
            // Close delete modal if open
            if (deleteModal && deleteModal.classList.contains('show')) {
                hideDeleteModal();
            }
            // Close event form if open
            const eventFormContainer = document.getElementById('eventFormContainer');
            if (eventFormContainer && eventFormContainer.classList.contains('show')) {
                hideEventForm();
            }
        }
    });

    // Auto-show event form if there was an error
    const eventFormError = document.querySelector('.event-form-error');
    const eventFormContainer = document.getElementById('eventFormContainer');
    if (eventFormError && eventFormContainer) {
        // Form should already be shown via Thymeleaf, but ensure it's visible
        eventFormContainer.classList.add('show');
        const createBtn = document.getElementById('createEventBtn');
        if (createBtn) {
            createBtn.style.display = 'none';
        }
    }
});