let userIdToDelete = null;
let tooltipTimeout;
let hideTimeout;
let currentTooltip = null;
let isTooltipHovered = false;

function showTooltip(element) {
    // Clear any existing timeouts
    if (tooltipTimeout) {
        clearTimeout(tooltipTimeout);
    }
    if (hideTimeout) {
        clearTimeout(hideTimeout);
    }

    // Set timeout for 1 second
    tooltipTimeout = setTimeout(() => {
        // Remove any existing tooltip
        hideTooltipImmediate();

        // Create tooltip content
        const userId = element.dataset.userId;
        const userEmail = element.dataset.userEmail;
        const userPhone = element.dataset.userPhone;
        const userRole = element.dataset.userRole;
        const userDate = element.dataset.userDate;
        const userName = element.textContent;

        const tooltipContent = `
          <div class="tooltip-row"><span class="tooltip-label">UserID:</span> ${userId}</div>
          <div class="tooltip-row"><span class="tooltip-label">FullName:</span> ${userName}</div>
          <div class="tooltip-row"><span class="tooltip-label">Email:</span> ${userEmail}</div>
          <div class="tooltip-row"><span class="tooltip-label">Phone:</span> ${userPhone}</div>
          <div class="tooltip-row"><span class="tooltip-label">Role:</span> ${userRole.toUpperCase()}</div>
          <div class="tooltip-row"><span class="tooltip-label">Member Since:</span> ${userDate}</div>
        `;

        // Create tooltip element
        const tooltip = document.createElement('div');
        tooltip.className = 'tooltip';
        tooltip.innerHTML = tooltipContent;

        // Add mouse events to tooltip itself
        tooltip.addEventListener('mouseenter', () => {
            isTooltipHovered = true;
            if (hideTimeout) {
                clearTimeout(hideTimeout);
            }
        });

        tooltip.addEventListener('mouseleave', () => {
            isTooltipHovered = false;
            hideTooltipDelayed();
        });

        // Position tooltip
        const rect = element.getBoundingClientRect();
        tooltip.style.left = (rect.left + window.scrollX) + 'px';
        tooltip.style.top = (rect.bottom + window.scrollY + 5) + 'px';

        // Add to document
        document.body.appendChild(tooltip);
        currentTooltip = tooltip;

        // Show tooltip with animation
        setTimeout(() => {
            if (currentTooltip === tooltip) {
                tooltip.classList.add('show');
            }
        }, 10);
    }, 1000); // 1-second delay
}

function hideTooltip(element) {
    // Clear show timeout
    if (tooltipTimeout) {
        clearTimeout(tooltipTimeout);
        tooltipTimeout = null;
    }

    // Don't hide immediately, use a small delay to allow moving to tooltip
    hideTooltipDelayed();
}

function hideTooltipDelayed() {
    // Clear any existing hide timeout
    if (hideTimeout) {
        clearTimeout(hideTimeout);
    }

    // Set a small delay before hiding to allow mouse movement to tooltip
    hideTimeout = setTimeout(() => {
        if (!isTooltipHovered) {
        hideTooltipImmediate();
        }
    }, 100); // Small delay to allow mouse movement
}

function hideTooltipImmediate() {
    // Clear all timeouts
    if (tooltipTimeout) {
        clearTimeout(tooltipTimeout);
        tooltipTimeout = null;
    }
    if (hideTimeout) {
        clearTimeout(hideTimeout);
        hideTimeout = null;
    }
    // Remove current tooltip
    if (currentTooltip) {
        currentTooltip.remove();
        currentTooltip = null;
    }
    isTooltipHovered = false;
}

// Edit Modal Functions
function showEditModal(userId, firstName, lastName, role) {
    document.getElementById('editUserId').value = userId;
    document.getElementById('editFirstName').value = firstName.charAt(0).toUpperCase() + firstName.slice(1);
    document.getElementById('editLastName').value = lastName.charAt(0).toUpperCase() + lastName.slice(1);
    document.getElementById('editRole').value = role;
    document.getElementById('editModal').style.display = 'block';
}

function closeEditModal() {
    document.getElementById('editModal').style.display = 'none';
}

// Delete Modal Functions
function showDeleteModal(userId, userName) {
    userIdToDelete = userId;
    document.getElementById('deleteMessage').textContent =
    'Are you sure you want to delete ' + userName + '? This action cannot be undone.';
    document.getElementById('deleteModal').style.display = 'block';
}

function confirmDelete() {
    if (userIdToDelete) {
        document.getElementById('userIdToDelete').value = userIdToDelete;
        document.getElementById('deleteForm').submit();
    }
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    userIdToDelete = null;
}

// Critical Action Functions
function showCriticalModal() {
    document.getElementById('criticalModal').style.display = 'block';
    document.getElementById('adminPassword').focus();
}

function closeCriticalModal() {
    document.getElementById('criticalModal').style.display = 'none';
    document.getElementById('adminPassword').value = '';
}

function showFinalConfirmation() {
    const password = document.getElementById('adminPassword').value;
    if (!password.trim()) {
        alert('Please enter your password!');
    return;
    }
    // Verify password with server before showing final confirmation
    verifyPasswordAndShowConfirmation(password);
}

function verifyPasswordAndShowConfirmation(password) {
    // Use fetch to verify password without page reload
    fetch('/admin/verify-password', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'adminPassword=' + encodeURIComponent(password)
    })
        .then(response => response.text())
        .then(result => {
            if (result === 'valid') {
                document.getElementById('criticalModal').style.display = 'none';
                document.getElementById('finalConfirmModal').style.display = 'block';
            } else {
                alert('Invalid password! Please try again.');
                document.getElementById('adminPassword').focus();
            }
        })
        .catch(error => {
            alert('Error verifying password. Please try again.');
            console.error('Password verification error:', error);
        });
}

function closeFinalConfirmModal() {
    document.getElementById('finalConfirmModal').style.display = 'none';
}

function executeDeleteAll() {
    // Password already verified, just submit the form directly
    document.getElementById('criticalForm').submit();
}

// Close modals when clicking outside (updated to include critical modals)
    window.onclick = function(event) {
    const editModal = document.getElementById('editModal');
    const deleteModal = document.getElementById('deleteModal');
    const criticalModal = document.getElementById('criticalModal');
    const finalConfirmModal = document.getElementById('finalConfirmModal');

    if (event.target === editModal) {
        closeEditModal();
    }
    if (event.target === deleteModal) {
        closeDeleteModal();
    }
    if (event.target === criticalModal) {
        closeCriticalModal();
    }
    if (event.target === finalConfirmModal) {
        closeFinalConfirmModal();
    }
}

// User Filtering Functions
function filterUsersByRole() {
    const filterValue = document.getElementById('roleFilter').value;
    const userItems = document.querySelectorAll('.user-item');
    const totalUserCount = userItems.length;
    let visibleCount = 0;

    // Filter user items
    userItems.forEach(userItem => {
        const userRole = userItem.getAttribute('data-user-role-filter');

        if (filterValue === 'all' || userRole === filterValue) {
            userItem.classList.remove('hidden');
            userItem.style.display = 'flex'; // Show the item
            visibleCount++;
        } else {
            userItem.classList.add('hidden');
            userItem.style.display = 'none'; // Hide the item
        }
    });

    // Update user count display
    updateUserCountDisplay(totalUserCount, visibleCount, filterValue);

    // Show/hide no users message
    updateNoUsersMessage(visibleCount);
}

function updateUserCountDisplay(totalCount, visibleCount, filterValue) {
    const totalUserCountElement = document.getElementById('totalUserCount');
    const filteredUserCountElement = document.getElementById('filteredUserCount');

    if (filterValue === 'all') {
        // Show only total count
        totalUserCountElement.textContent = totalCount;
        filteredUserCountElement.style.display = 'none';
    } else {
        // Show total and filtered count
        totalUserCountElement.textContent = totalCount;
        filteredUserCountElement.style.display = 'inline';
        filteredUserCountElement.textContent = ` (Showing ${visibleCount} ${capitalizeRole(filterValue)}${visibleCount !== 1 ? 's' : ''})`;
    }
}

function updateNoUsersMessage(visibleCount) {
    const noUsersMessage = document.getElementById('noFilteredUsers');
    const userListContainer = document.querySelector('.user-list-section > div:nth-child(3)'); // The div with user items

    if (visibleCount === 0 && userListContainer) {
        // Show "no filtered users" message
        if (!document.getElementById('noFilteredUsersMessage')) {
            const noFilteredMessage = document.createElement('div');
            noFilteredMessage.id = 'noFilteredUsersMessage';
            noFilteredMessage.className = 'no-users';
            noFilteredMessage.textContent = 'No users found with the selected role.';
            userListContainer.parentNode.appendChild(noFilteredMessage);
        } else {
            document.getElementById('noFilteredUsersMessage').style.display = 'block';
        }
    } else {
        // Hide "no filtered users" message
        const noFilteredMessage = document.getElementById('noFilteredUsersMessage');
        if (noFilteredMessage) {
            noFilteredMessage.style.display = 'none';
        }
    }
}

function capitalizeRole(role) {
    return role.charAt(0).toUpperCase() + role.slice(1);
}

// Initialize filter on page load
document.addEventListener('DOMContentLoaded', function() {
    // Set default filter value
    const roleFilter = document.getElementById('roleFilter');
    if (roleFilter) {
        roleFilter.value = 'all';
        filterUsersByRole(); // Apply initial filter
    }
});