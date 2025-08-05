// User Items Scripts

// Global variables for return functionality
let currentItemId = null;
let currentItemName = null;

// Global variables for filtering and sorting
let allItems = [];
let filteredItems = [];

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
            if (typeof Toast !== 'undefined') {
                if (toastType === 'success') {
                    Toast.success(toastMessage);
                } else {
                    Toast.error(toastMessage);
                }
            }
        }, 300);
    }

    // Initialize filtering and sorting
    initializeFiltersAndSorting();
});

/**
 * Initialize filtering and sorting functionality
 */
function initializeFiltersAndSorting() {
    // Collect all items data from the table
    collectItemsData();

    // Populate responsibility filter dropdown
    populateResponsibilityFilter();

    // Add event listeners
    addFilterSortListeners();

    // Update item count
    updateItemCount();
}

/**
 * Collect items data from the table for filtering/sorting
 */
function collectItemsData() {
    allItems = [];
    const rows = document.querySelectorAll('.items-table tbody tr');

    rows.forEach((row, index) => {
        const itemName = row.querySelector('td:nth-child(1)').textContent.trim();
        const responsibilityName = row.querySelector('td:nth-child(2)').textContent.trim();

        allItems.push({
            index: index,
            element: row,
            itemName: itemName.toLowerCase(),
            itemNameDisplay: itemName,
            responsibilityName: responsibilityName.toLowerCase(),
            responsibilityNameDisplay: responsibilityName
        });
    });

    filteredItems = [...allItems];
}

/**
 * Populate the responsibility filter dropdown with unique values
 */
function populateResponsibilityFilter() {
    const filterDropdown = document.getElementById('responsibilityFilter');
    if (!filterDropdown) return;

    // Get unique responsibility names
    const uniqueResponsibilities = [...new Set(allItems.map(item => item.responsibilityNameDisplay))];
    uniqueResponsibilities.sort();

    // Clear existing options (except "All Responsibilities")
    filterDropdown.innerHTML = '<option value="">All Responsibilities</option>';

    // Add unique responsibility options
    uniqueResponsibilities.forEach(responsibility => {
        const option = document.createElement('option');
        option.value = responsibility;
        option.textContent = responsibility;
        filterDropdown.appendChild(option);
    });
}

/**
 * Add event listeners for filters and sorting
 */
function addFilterSortListeners() {
    // Responsibility filter
    const responsibilityFilter = document.getElementById('responsibilityFilter');
    if (responsibilityFilter) {
        responsibilityFilter.addEventListener('change', applyFiltersAndSorting);
    }

    // Sort dropdown
    const sortDropdown = document.getElementById('sortItems');
    if (sortDropdown) {
        sortDropdown.addEventListener('change', applyFiltersAndSorting);
    }

    // Search input
    const searchInput = document.getElementById('searchItems');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(applyFiltersAndSorting, 300));
    }
}

/**
 * Apply filters and sorting
 */
function applyFiltersAndSorting() {
    // Start with all items
    filteredItems = [...allItems];

    // Apply responsibility filter
    applyResponsibilityFilter();

    // Apply search filter
    applySearchFilter();

    // Apply sorting
    applySorting();

    // Update display
    updateItemsDisplay();

    // Update count
    updateItemCount();
}

/**
 * Apply responsibility filter
 */
function applyResponsibilityFilter() {
    const responsibilityFilter = document.getElementById('responsibilityFilter');
    if (!responsibilityFilter) return;

    const selectedResponsibility = responsibilityFilter.value.toLowerCase();

    if (selectedResponsibility) {
        filteredItems = filteredItems.filter(item =>
            item.responsibilityName === selectedResponsibility
        );
    }
}

/**
 * Apply search filter (searches both item name and responsibility name)
 */
function applySearchFilter() {
    const searchInput = document.getElementById('searchItems');
    if (!searchInput) return;

    const searchTerm = searchInput.value.toLowerCase().trim();

    if (searchTerm) {
        filteredItems = filteredItems.filter(item =>
            item.itemName.includes(searchTerm) ||
            item.responsibilityName.includes(searchTerm)
        );
    }
}

/**
 * Apply sorting
 */
function applySorting() {
    const sortDropdown = document.getElementById('sortItems');
    if (!sortDropdown) return;

    const sortValue = sortDropdown.value;

    switch (sortValue) {
        case 'name-asc':
            filteredItems.sort((a, b) => a.itemName.localeCompare(b.itemName));
            break;
        case 'name-desc':
            filteredItems.sort((a, b) => b.itemName.localeCompare(a.itemName));
            break;
        default:
            // Keep original order
            filteredItems.sort((a, b) => a.index - b.index);
    }
}

/**
 * Update the items display based on filtered results
 */
function updateItemsDisplay() {
    // Hide all items first
    allItems.forEach(item => {
        item.element.classList.add('hidden');
    });

    // Show filtered items
    filteredItems.forEach(item => {
        item.element.classList.remove('hidden');
    });

    // Show/hide no filtered items message
    const noFilteredMessage = document.getElementById('noFilteredItems');
    const tableContainer = document.querySelector('.items-table-container');

    if (filteredItems.length === 0 && allItems.length > 0) {
        if (noFilteredMessage) {
            noFilteredMessage.classList.add('show');
        }
        if (tableContainer) {
            tableContainer.style.display = 'none';
        }
    } else {
        if (noFilteredMessage) {
            noFilteredMessage.classList.remove('show');
        }
        if (tableContainer) {
            tableContainer.style.display = 'block';
        }
    }
}

/**
 * Update item count display
 */
function updateItemCount() {
    const filteredCountElement = document.getElementById('filteredItemCount');
    const totalCountElement = document.getElementById('totalItemCount');

    if (filteredCountElement) {
        filteredCountElement.textContent = filteredItems.length;
    }

    if (totalCountElement) {
        totalCountElement.textContent = allItems.length;
    }
}

/**
 * Debounce function to limit search input frequency
 */
function debounce(func, delay) {
    let timeoutId;
    return function (...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => func.apply(this, args), delay);
    };
}

/**
 * Clear all filters and search
 */
function clearAllFilters() {
    // Reset dropdowns
    const responsibilityFilter = document.getElementById('responsibilityFilter');
    const sortDropdown = document.getElementById('sortItems');
    const searchInput = document.getElementById('searchItems');

    if (responsibilityFilter) responsibilityFilter.value = '';
    if (sortDropdown) sortDropdown.value = '';
    if (searchInput) searchInput.value = '';

    // Reapply filters (which will now show all items)
    applyFiltersAndSorting();
}

// ======================
// RETURN ITEM FUNCTIONALITY
// ======================

/**
 * Show return confirmation modal
 */
function showReturnModal(itemId, itemName) {
    currentItemId = itemId;
    currentItemName = itemName;

    document.getElementById('returnMessage').textContent =
        `Are you sure you want to request to return "${itemName}"?`;
    document.getElementById('returnModal').style.display = 'block';
}

/**
 * Close return modal
 */
function closeReturnModal() {
    document.getElementById('returnModal').style.display = 'none';
    currentItemId = null;
    currentItemName = null;
}

/**
 * Confirm item return
 */
function confirmItemReturn() {
    if (!currentItemId) {
        return;
    }

    const confirmBtn = document.getElementById('confirmReturnBtn');
    const originalText = confirmBtn.textContent;

    // Disable button and show loading
    confirmBtn.disabled = true;
    confirmBtn.textContent = 'Requesting...';

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
                // Refresh the page to update the items list
                window.location.reload();
            } else {
                // Show error immediately (no page refresh needed)
                if (typeof Toast !== 'undefined') {
                    Toast.error(message);
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
            if (typeof Toast !== 'undefined') {
                Toast.error(errorMessage);
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
    const modal = document.getElementById('returnModal');
    if (event.target === modal) {
        closeReturnModal();
    }
}

/**
 * Close modal with Escape key
 */
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeReturnModal();
    }
});