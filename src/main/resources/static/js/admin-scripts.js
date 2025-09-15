/* Clean admin-scripts.js - Truly Minimal */

// Chart instances and data
let charts = { tab1: null, tab2: null, tab3: null };
let metricsData = {};

// Chart configurations
const chartConfigs = {
    tab1: {
        url: '/admin/metrics/user-roles',
        chartId: 'userRoleChart',
        dataKey: 'roleCounts',
        totalKey: 'totalUsers',
        legends: [
            { id: 'chiefCount', key: 'chief', label: 'Chief', color: '#007bff' },
            { id: 'managerCount', key: 'manager', label: 'Manager', color: '#28a745' },
            { id: 'userCount', key: 'user', label: 'User', color: '#fd7e14' }
        ],
        totalId: 'totalUserCount',
        emptyMessage: 'No users in system'
    },
    tab2: {
        url: '/admin/metrics/item-status',
        chartId: 'itemStatusChart',
        dataKey: 'statusCounts',
        totalKey: 'totalItems',
        legends: [
            { id: 'availableCount', key: 'available', label: 'Available', color: '#28a745' },
            { id: 'inUseCount', key: 'inUse', label: 'In Use', color: '#ffc107' },
            { id: 'unavailableCount', key: 'unavailable', label: 'Unavailable', color: '#dc3545' }
        ],
        totalId: 'totalItemCount',
        emptyMessage: 'No items in system'
    },
    tab3: {
        url: '/admin/metrics/event-status',
        chartId: 'eventStatusChart',
        dataKey: 'statusCounts',
        totalKey: 'totalEvents',
        legends: [
            { id: 'notActiveCount', key: 'notActive', label: 'Not Active', color: '#6c757d' },
            { id: 'activeCount', key: 'active', label: 'Active', color: '#007bff' },
            { id: 'equipmentReturnCount', key: 'equipmentReturn', label: 'Equipment Return', color: '#17a2b8' }
        ],
        totalId: 'totalEventCount',
        emptyMessage: 'No events in system'
    }
};

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadAllMetricsData();
    switchTab('tab1');
});

// Load data for all metrics
async function loadAllMetricsData() {
    try {
        const promises = Object.values(chartConfigs).map(config =>
            fetch(config.url).then(r => r.json())
        );
        const [userData, itemData, eventData] = await Promise.all(promises);
        metricsData = { tab1: userData, tab2: itemData, tab3: eventData };
    } catch (error) {
        console.error('Error loading metrics:', error);
    }
}

// Create chart for active tab
function createChartForActiveTab(tabId) {
    if (!charts[tabId] && metricsData[tabId]) {
        updateChart(tabId, metricsData[tabId]);
    }
}

// Generic chart update function
function updateChart(tabId, data) {
    const config = chartConfigs[tabId];
    const counts = data[config.dataKey];
    const total = data[config.totalKey];

    // Update legends
    config.legends.forEach(legend => {
        document.getElementById(legend.id).textContent = counts[legend.key];
    });
    document.getElementById(config.totalId).textContent = total;

    // Create chart
    const ctx = document.getElementById(config.chartId).getContext('2d');
    if (charts[tabId]) charts[tabId].destroy();

    if (total === 0) {
        charts[tabId] = createEmptyChart(ctx, config.emptyMessage);
        return;
    }

    const chartData = config.legends
        .filter(legend => counts[legend.key] > 0)
        .map(legend => ({
            data: counts[legend.key],
            label: legend.label,
            color: legend.color
        }));

    charts[tabId] = createChart(ctx, chartData, total);
}

// Create pie chart
function createChart(ctx, items, total) {
    return new Chart(ctx, {
        type: 'pie',
        data: {
            labels: items.map(item => item.label),
            datasets: [{
                data: items.map(item => item.data),
                backgroundColor: items.map(item => item.color),
                borderColor: '#ffffff',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#333333',
                    titleColor: '#ffffff',
                    bodyColor: '#ffffff',
                    callbacks: {
                        label: function(context) {
                            const percentage = ((context.parsed / total) * 100).toFixed(1);
                            return `${context.label}: ${context.parsed} (${percentage}%)`;
                        }
                    }
                }
            },
            animation: {
                animateRotate: true,
                animateScale: true,
                duration: 1200,
                easing: 'easeInOutQuart'
            }
        }
    });
}

// Create empty chart
function createEmptyChart(ctx, message) {
    const chart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ['No Data'],
            datasets: [{ data: [1], backgroundColor: ['#e9ecef'], borderColor: ['#dee2e6'], borderWidth: 2 }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: { legend: { display: false }, tooltip: { enabled: false } }
        }
    });

    const wrapper = ctx.canvas.closest('.chart-wrapper');
    if (wrapper && !wrapper.querySelector('.empty-chart-message')) {
        const overlay = document.createElement('div');
        overlay.className = 'empty-chart-message';
        overlay.textContent = message;
        overlay.style.cssText = `
            position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
            color: #6c757d; font-weight: 500; pointer-events: none; text-align: center;
        `;
        wrapper.appendChild(overlay);
    }

    return chart;
}

// Switch tabs
function switchTab(tabId) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-button').forEach(btn => btn.classList.remove('active'));

    document.getElementById(tabId).classList.add('active');
    document.querySelector(`[data-tab="${tabId}"]`).classList.add('active');

    setTimeout(() => createChartForActiveTab(tabId), 100);
}

// User Management Variables
let allUsers = [];
let filteredUsers = [];

// Load user management data
async function loadUserManagementData() {
    try {
        const response = await fetch('/admin/user-management');
        const data = await response.json();

        if (data.error) {
            showUserTableError(data.error);
            return;
        }

        allUsers = data.users || [];
        filteredUsers = [...allUsers];
        renderUserTable();

    } catch (error) {
        console.error('Error loading user management data:', error);
        showUserTableError('Failed to load users');
    }
}

// Render user table - Updated for email column
function renderUserTable() {
    const loadingElement = document.getElementById('userTableLoading');
    const emptyElement = document.getElementById('userTableEmpty');
    const tableWrapperElement = document.getElementById('userTableWrapper');
    const tbody = document.getElementById('userTableBody');

    // Hide loading
    loadingElement.style.display = 'none';

    if (filteredUsers.length === 0) {
        tableWrapperElement.style.display = 'none';
        emptyElement.style.display = 'block';
        return;
    }

    // Show table wrapper and populate
    emptyElement.style.display = 'none';
    tableWrapperElement.style.display = 'block';

    tbody.innerHTML = filteredUsers.map(user => `
        <tr data-user-id="${user.userId}">
            <td class="user-name">${user.fullName}</td>
            <td class="user-email">${user.email}</td>
            <td class="user-phone">${user.phone}</td>
            <td class="user-role">
                <span class="role-badge role-${user.role.toLowerCase()}">${user.role}</span>
            </td>
            <td class="user-actions">
                ${user.isChief
        ? `<button class="btn-demote" onclick="demoteUser(${user.userId})">Remove Chief</button>`
        : `<button class="btn-promote" onclick="promoteUser(${user.userId})">Make Chief</button>`
    }
                <button class="btn-delete" onclick="showDeleteUserModal(${user.userId}, '${user.fullName}', '${user.role}')">Delete</button>
            </td>
        </tr>
    `).join('');
}

// Show error in user table - Enhanced empty state
function showUserTableError(message) {
    const loadingElement = document.getElementById('userTableLoading');
    const tableWrapperElement = document.getElementById('userTableWrapper');
    const emptyElement = document.getElementById('userTableEmpty');

    loadingElement.style.display = 'none';
    tableWrapperElement.style.display = 'none';
    emptyElement.style.display = 'block';

    // Create enhanced empty state content
    emptyElement.innerHTML = `
        <div class="empty-icon"></div>
        <h4 class="empty-title">No Users Found</h4>
        <p class="empty-subtitle">${message === 'No users found' ? 'Try adjusting your search or filter criteria' : message}</p>
    `;
}

// Promote user to chief
async function promoteUser(userId) {
    if (!confirm('Are you sure you want to promote this user to Chief?')) {
        return;
    }

    try {
        const formData = new FormData();
        formData.append('userId', userId);

        const response = await fetch('/admin/promote-chief', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (result.success) {
            Toast.success(result.message);
            await loadUserManagementData(); // Refresh table
            await loadAllMetricsData(); // Refresh metrics charts
        } else {
            Toast.error(result.message);
        }

    } catch (error) {
        console.error('Error promoting user:', error);
        Toast.error('Failed to promote user');
    }
}

// Demote chief to user
async function demoteUser(userId) {
    try {
        // Check if this is the last chief
        const checkResponse = await fetch(`/admin/check-last-chief?userId=${userId}`);
        const checkResult = await checkResponse.json();

        let confirmMessage = 'Are you sure you want to remove Chief role from this user?';
        if (checkResult.isLastChief) {
            confirmMessage = 'WARNING: This is the last Chief in the system! Are you sure you want to remove their Chief role? The system will have no Chiefs after this action.';
        }

        if (!confirm(confirmMessage)) {
            return;
        }

        // Proceed with demotion
        const formData = new FormData();
        formData.append('userId', userId);

        const response = await fetch('/admin/demote-chief', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (result.success) {
            let message = result.message;
            if (result.wasLastChief) {
                message += ' (System now has no Chiefs)';
            }
            Toast.success(result.message);
            await loadUserManagementData(); // Refresh table
            await loadAllMetricsData(); // Refresh metrics charts
        } else {
            Toast.error(result.message);
        }

    } catch (error) {
        console.error('Error demoting chief:', error);
        Toast.error('Failed to demote chief');
    }
}

// Search and filter functionality
function setupUserManagementControls() {
    const searchInput = document.getElementById('userSearch');
    const roleFilter = document.getElementById('roleFilter');

    if (searchInput) {
        searchInput.addEventListener('input', filterUsers);
    }

    if (roleFilter) {
        roleFilter.addEventListener('change', filterUsers);
    }
}

// Updated filter function to include email in search
function filterUsers() {
    const searchTerm = document.getElementById('userSearch').value.toLowerCase();
    const roleFilter = document.getElementById('roleFilter').value;

    filteredUsers = allUsers.filter(user => {
        // Search filter - now includes email
        const matchesSearch = searchTerm === '' ||
            user.fullName.toLowerCase().includes(searchTerm) ||
            user.email.toLowerCase().includes(searchTerm) ||
            user.phone.includes(searchTerm);

        // Role filter
        let matchesRole = true;
        if (roleFilter === 'chief') {
            matchesRole = user.isChief;
        } else if (roleFilter === 'non-chief') {
            matchesRole = !user.isChief;
        }

        return matchesSearch && matchesRole;
    });

    renderUserTable();
}

// Update the existing switchTab function to handle user management
const originalSwitchTab = switchTab;
switchTab = function(tabId) {
    originalSwitchTab(tabId);

    // Load user management data when tab 1 is activated
    if (tabId === 'tab1') {
        setTimeout(() => {
            loadUserManagementData();
            setupUserManagementControls();
        }, 200); // Small delay to ensure chart loads first
    }
};

// Delete User Modal Management
let userIdToDelete = null;

// Show delete user modal
function showDeleteUserModal(userId, userName, userRole) {
    userIdToDelete = userId;

    // Set user details in modal
    document.getElementById('deleteUserName').textContent = userName;
    const roleElement = document.getElementById('deleteUserRole');
    roleElement.textContent = userRole;
    roleElement.className = `role-badge role-${userRole.toLowerCase()}`;

    // Show modal
    document.getElementById('deleteUserModal').classList.add('show');
}

// Hide delete user modal
function hideDeleteUserModal() {
    document.getElementById('deleteUserModal').classList.remove('show');
    userIdToDelete = null;
}

// Confirm delete user
async function confirmDeleteUser() {
    if (!userIdToDelete) {
        Toast.error('No user selected for deletion');
        return;
    }

    try {
        const formData = new FormData();
        formData.append('userId', userIdToDelete);

        const response = await fetch('/admin/delete-user', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        // Hide modal first
        hideDeleteUserModal();

        if (result.success) {
            Toast.success(result.message);
            await loadUserManagementData(); // Refresh user table
            await loadAllMetricsData(); // Refresh metrics charts
        } else {
            Toast.error(result.message);
        }

    } catch (error) {
        hideDeleteUserModal();
        console.error('Error deleting user:', error);
        Toast.error('Failed to delete user');
    }
}

// Close modal when clicking outside or pressing ESC
document.addEventListener('DOMContentLoaded', function() {
    // Close modal when clicking outside
    const deleteModal = document.getElementById('deleteUserModal');
    if (deleteModal) {
        deleteModal.addEventListener('click', function(e) {
            if (e.target === this) {
                hideDeleteUserModal();
            }
        });
    }

    // Handle ESC key to close modal
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            const deleteModal = document.getElementById('deleteUserModal');
            if (deleteModal && deleteModal.classList.contains('show')) {
                hideDeleteUserModal();
            }
        }
    });
});

// Add these placeholder functions to the end of admin-scripts.js

// ========== ADMIN USER ACTIONS (BULK OPERATIONS) ==========

/**
 * Placeholder function for deleting all non-admin users
 */
function deleteAllUsers() {
    console.log('Delete All Users clicked - Implementation pending');
    Toast.info('Delete All Users functionality will be implemented next');
}

/**
 * Placeholder function for making all managers have "User" role
 */
function makeAllManagersUser() {
    console.log('Make All Managers "User" Role clicked - Implementation pending');
    Toast.info('Manager demotion functionality will be implemented next');
}

/**
 * Placeholder function for making all chiefs have "User" role
 */
function makeAllChiefsUser() {
    console.log('Make All Chiefs "User" Role clicked - Implementation pending');
    Toast.info('Chief demotion functionality will be implemented next');
}