// Chief User List JavaScript

// Initialize and check for flash messages on page load
document.addEventListener('DOMContentLoaded', function() {
    // Check for success/error messages from server
    const urlParams = new URLSearchParams(window.location.search);
    const successMsg = urlParams.get('success');
    const errorMsg = urlParams.get('error');

    if (successMsg) {
        Toast.success(decodeURIComponent(successMsg));
    }
    if (errorMsg) {
        Toast.error(decodeURIComponent(errorMsg));
    }

    // Handle Enter key in assign form
    const responsibilityInput = document.getElementById('responsibilityName');
    if (responsibilityInput) {
        responsibilityInput.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                confirmAssign();
            }
        });
    }
});

// Filter and Search Users
function filterUsers() {
    const roleFilter = document.getElementById('roleFilter').value.toLowerCase();
    const searchInput = document.getElementById('userSearch').value.toLowerCase();
    const userItems = document.querySelectorAll('.user-item');
    const noFilteredUsers = document.getElementById('noFilteredUsers');

    let visibleCount = 0;
    const totalCount = userItems.length;

    userItems.forEach(item => {
        const userRole = item.getAttribute('data-user-role-filter').toLowerCase();
        const userName = item.getAttribute('data-user-search').toLowerCase();

        // Check role filter
        const roleMatch = roleFilter === 'all' || userRole === roleFilter;

        // Check search filter
        const searchMatch = userName.includes(searchInput);

        // Show or hide user item
        if (roleMatch && searchMatch) {
            item.classList.remove('hidden');
            visibleCount++;
        } else {
            item.classList.add('hidden');
        }
    });

    // Update count display
    const totalUserCount = document.getElementById('totalUserCount');
    const filteredUserCount = document.getElementById('filteredUserCount');

    if (visibleCount < totalCount) {
        filteredUserCount.textContent = ` (Showing ${visibleCount})`;
        filteredUserCount.style.display = 'inline';
    } else {
        filteredUserCount.style.display = 'none';
    }

    // Show/hide no results message
    if (visibleCount === 0 && totalCount > 0) {
        noFilteredUsers.classList.add('show');
        noFilteredUsers.innerHTML = '<h3>No users found</h3><p>Try adjusting your filters or search query.</p>';
    } else {
        noFilteredUsers.classList.remove('show');
    }
}

// Assign Responsibility Modal Functions
function showAssignModal(userId, userName) {
    const modal = document.getElementById('assignModal');
    const message = document.getElementById('assignMessage');
    const userIdInput = document.getElementById('assignUserId');
    const responsibilityInput = document.getElementById('responsibilityName');

    // Set user info
    message.textContent = `Assign responsibility to: ${userName}`;
    userIdInput.value = userId;
    responsibilityInput.value = '';

    // Show modal
    modal.classList.add('show');
    modal.style.display = 'flex';

    // Focus on input
    setTimeout(() => responsibilityInput.focus(), 100);
}

function closeAssignModal() {
    const modal = document.getElementById('assignModal');
    modal.classList.remove('show');
    modal.style.display = 'none';
}

function confirmAssign() {
    const form = document.getElementById('assignForm');
    const responsibilityInput = document.getElementById('responsibilityName');

    // Validate input
    if (!responsibilityInput.value.trim()) {
        alert('Please enter a responsibility name');
        responsibilityInput.focus();
        return;
    }

    // Submit form
    form.submit();
}

// Remove Responsibility Modal Functions
function showRemoveModal(userId, userName, responsibilityName) {
    const modal = document.getElementById('removeModal');
    const message = document.getElementById('removeMessage');
    const userIdInput = document.getElementById('removeUserId');

    // Set user info
    message.innerHTML = `Remove responsibility from: <strong>${userName}</strong><br>Responsibility: <strong>${responsibilityName}</strong>`;
    userIdInput.value = userId;

    // Show modal
    modal.classList.add('show');
    modal.style.display = 'flex';
}

function closeRemoveModal() {
    const modal = document.getElementById('removeModal');
    modal.classList.remove('show');
    modal.style.display = 'none';
}

function confirmRemove() {
    const form = document.getElementById('removeForm');
    form.submit();
}

// Close modals when clicking outside
window.onclick = function(event) {
    const assignModal = document.getElementById('assignModal');
    const removeModal = document.getElementById('removeModal');

    if (event.target === assignModal) {
        closeAssignModal();
    }
    if (event.target === removeModal) {
        closeRemoveModal();
    }
}

// Close modals with Escape key
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeAssignModal();
        closeRemoveModal();
    }
});