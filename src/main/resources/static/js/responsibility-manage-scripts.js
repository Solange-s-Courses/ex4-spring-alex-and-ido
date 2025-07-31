// Responsibility Management Scripts

// Description validation and character counter
function updateCharCounter() {
    const textarea = document.getElementById('descriptionText');
    const counter = document.getElementById('charCounter');
    const currentLength = textarea.value.length;

    counter.textContent = currentLength + '/200 characters';

    // Change color based on character count
    if (currentLength >= 180) {
        counter.className = 'char-counter danger';
    } else if (currentLength >= 150) {
        counter.className = 'char-counter warning';
    } else {
        counter.className = 'char-counter';
    }
}

// Validation function
function validateItemName(itemName) {
    if (!itemName || itemName.trim().length === 0) {
        return "Item name is required";
    }

    itemName = itemName.trim();

    if (itemName.length > 32) {
        return "Item name cannot exceed 32 characters";
    }

    // Allow letters, numbers, and spaces
    const pattern = /^[A-Za-z0-9 .#()-]+$/;
    if (!pattern.test(itemName)) {
        return "Item name can only contain letters, numbers, spaces and the .#()- symbols";
    }

    return null; // No error
}

// Show error in modal
function showModalError(modalType, message) {
    const errorDiv = document.getElementById(modalType + 'Error');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
}

// Hide error in modal
function hideModalError(modalType) {
    const errorDiv = document.getElementById(modalType + 'Error');
    errorDiv.style.display = 'none';
}

// Edit Description Modal Functions
function showEditDescriptionModal() {
    hideModalError('editDescription');
    document.getElementById('editDescriptionModal').style.display = 'block';
    const textarea = document.getElementById('descriptionText');
    textarea.focus();
    updateCharCounter();

    // Add event listener for character counter
    textarea.addEventListener('input', updateCharCounter);
}

function closeEditDescriptionModal() {
    document.getElementById('editDescriptionModal').style.display = 'none';
    hideModalError('editDescription');
}

function confirmEditDescription() {
    const description = document.getElementById('descriptionText').value;

    // Validate description length
    if (description.length > 200) {
        showModalError('editDescription', 'Description cannot exceed 200 characters');
        return;
    }

    // Hide error and submit
    hideModalError('editDescription');
    document.getElementById('editDescriptionForm').submit();
}

// Add Item Modal Functions
function showAddItemModal() {
    hideModalError('addItem');
    document.getElementById('addItemModal').style.display = 'block';
    document.getElementById('addItemName').focus();
}

function closeAddItemModal() {
    document.getElementById('addItemModal').style.display = 'none';
    document.getElementById('addItemForm').reset();
    hideModalError('addItem');
}

function confirmAddItem() {
    const itemName = document.getElementById('addItemName').value;
    const status = document.getElementById('addItemStatus').value;

    // Validate item name
    const nameError = validateItemName(itemName);
    if (nameError) {
        showModalError('addItem', nameError);
        document.getElementById('addItemName').focus();
        return;
    }

    // Validate status
    if (!status) {
        showModalError('addItem', 'Please select a status');
        document.getElementById('addItemStatus').focus();
        return;
    }

    // Hide error and submit
    hideModalError('addItem');
    document.getElementById('addItemForm').submit();
}

// Edit Item Modal Functions
function showEditItemModal(itemId, itemName, itemStatus) {
    hideModalError('editItem');
    document.getElementById('editItemId').value = itemId;
    document.getElementById('editItemName').value = itemName;
    document.getElementById('editItemStatus').value = itemStatus;
    document.getElementById('editItemModal').style.display = 'block';
    document.getElementById('editItemName').focus();
}

function closeEditItemModal() {
    document.getElementById('editItemModal').style.display = 'none';
    document.getElementById('editItemForm').reset();
    hideModalError('editItem');
}

function confirmEditItem() {
    const itemName = document.getElementById('editItemName').value;
    const status = document.getElementById('editItemStatus').value;

    // Validate item name
    const nameError = validateItemName(itemName);
    if (nameError) {
        showModalError('editItem', nameError);
        document.getElementById('editItemName').focus();
        return;
    }

    // Validate status
    if (!status) {
        showModalError('editItem', 'Please select a status');
        document.getElementById('editItemStatus').focus();
        return;
    }

    // Hide error and submit
    hideModalError('editItem');
    document.getElementById('editItemForm').submit();
}

// Tab System Functions
function switchTab(tabName) {
    // Hide all tab contents
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => {
        content.classList.remove('active');
    });

    // Remove active class from all tab buttons
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(button => {
        button.classList.remove('active');
    });

    // Show selected tab content
    const selectedContent = document.getElementById(tabName + 'Content');
    if (selectedContent) {
        selectedContent.classList.add('active');
    }

    // Add active class to selected tab button
    const selectedButton = document.getElementById(tabName + 'Tab');
    if (selectedButton) {
        selectedButton.classList.add('active');
    }
}

// Initialize tabs on page load
document.addEventListener('DOMContentLoaded', function() {
    const itemsTab = document.getElementById('itemsTab');
    if (itemsTab) {
        // Check URL parameter for active tab
        const urlParams = new URLSearchParams(window.location.search);
        const activeTab = urlParams.get('tab');

        if (activeTab === 'requests') {
            switchTab('requests');
        } else {
            switchTab('items');
        }
    }
});

// Reusable Confirmation Modal Functions
let confirmationAction = null;
let confirmationData = {};

function showConfirmationModal(title, message, buttonText, buttonClass, action, data) {
    document.getElementById('confirmationTitle').textContent = title;
    document.getElementById('confirmationMessage').textContent = message;

    const button = document.getElementById('confirmationButton');
    button.textContent = buttonText;
    button.className = 'btn ' + buttonClass;

    confirmationAction = action;
    confirmationData = data;

    document.getElementById('confirmationModal').style.display = 'block';
}

function closeConfirmationModal() {
    document.getElementById('confirmationModal').style.display = 'none';
    confirmationAction = null;
    confirmationData = {};
}

function executeConfirmationAction() {
    if (confirmationAction) {
        confirmationAction(confirmationData);
    }
    closeConfirmationModal();
}

// Delete Item Function (Updated to use reusable modal)
function showDeleteItemModal(itemId, itemName) {
    const title = 'Delete Item';
    const message = `Are you sure you want to delete "${itemName}"? This action cannot be undone.`;
    const buttonText = 'Delete';
    const buttonClass = 'btn-remove';

    const action = (data) => {
        // Check if delete form exists (from the old modal system)
        const deleteForm = document.getElementById('deleteItemForm');
        const deleteItemIdInput = document.getElementById('deleteItemId');

        if (deleteForm && deleteItemIdInput) {
            // Use existing delete form
            deleteItemIdInput.value = data.itemId;
            deleteForm.submit();
        } else {
            // Use hidden action form as fallback
            const form = document.getElementById('hiddenActionForm');
            const input = document.getElementById('hiddenActionInput');

            if (form && input) {
                const responsibilityElement = document.querySelector('[data-responsibility-id]');
                const responsibilityId = responsibilityElement ? responsibilityElement.dataset.responsibilityId : '';

                form.action = `/responsibility-manage/${responsibilityId}/delete-item`;
                input.name = 'itemId';
                input.value = data.itemId;
                form.submit();
            } else {
                console.error('No form available for delete action');
            }
        }
    };

    showConfirmationModal(title, message, buttonText, buttonClass, action, { itemId });
}

function closeDeleteItemModal() {
    closeConfirmationModal();
}

function confirmDeleteItem() {
    executeConfirmationAction();
}

// Approve Request Function
function showApproveRequestModal(requestId, userName, itemName, requestType) {
    const actionText = requestType === 'request' ? 'give' : 'accept return of';
    const title = 'Approve Request';
    const message = `Are you sure you want to approve ${userName}'s request to ${actionText} "${itemName}"?`;
    const buttonText = 'Approve Request';
    const buttonClass = 'btn-assign';

    const action = (data) => {
        const form = document.getElementById('hiddenActionForm');
        const input = document.getElementById('hiddenActionInput');

        if (form && input) {
            const responsibilityElement = document.querySelector('[data-responsibility-id]');
            const responsibilityId = responsibilityElement ? responsibilityElement.dataset.responsibilityId : '';

            // Add activeTab parameter to form
            addTabParameterToForm(form);

            form.action = `/responsibility-manage/${responsibilityId}/approve-request`;
            input.name = 'requestId';
            input.value = data.requestId;
            form.submit();
        } else {
            console.error('Hidden form not found for approve action');
        }
    };

    showConfirmationModal(title, message, buttonText, buttonClass, action, { requestId });
}

// Deny Request Function
function showDenyRequestModal(requestId, userName, itemName, requestType) {
    const actionText = requestType === 'request' ? 'request for' : 'return request for';
    const title = 'Deny Request';
    const message = `Are you sure you want to deny ${userName}'s ${actionText} "${itemName}"?`;
    const buttonText = 'Deny Request';
    const buttonClass = 'btn-remove';

    const action = (data) => {
        const form = document.getElementById('hiddenActionForm');
        const input = document.getElementById('hiddenActionInput');

        if (form && input) {
            const responsibilityElement = document.querySelector('[data-responsibility-id]');
            const responsibilityId = responsibilityElement ? responsibilityElement.dataset.responsibilityId : '';

            // Add activeTab parameter to form
            addTabParameterToForm(form);

            form.action = `/responsibility-manage/${responsibilityId}/deny-request`;
            input.name = 'requestId';
            input.value = data.requestId;
            form.submit();
        } else {
            console.error('Hidden form not found for deny action');
        }
    };

    showConfirmationModal(title, message, buttonText, buttonClass, action, { requestId });
}

// Helper function to add activeTab parameter to form
function addTabParameterToForm(form) {
    // Check if activeTab input already exists, if not create it
    let activeTabInput = form.querySelector('input[name="activeTab"]');
    if (!activeTabInput) {
        activeTabInput = document.createElement('input');
        activeTabInput.type = 'hidden';
        activeTabInput.name = 'activeTab';
        form.appendChild(activeTabInput);
    }

    // Set value to 'requests' since we're in the requests tab
    activeTabInput.value = 'requests';
}

// Window click handler for modal backgrounds
window.onclick = function(event) {
    const addModal = document.getElementById('addItemModal');
    const editModal = document.getElementById('editItemModal');
    const descriptionModal = document.getElementById('editDescriptionModal');
    const confirmationModal = document.getElementById('confirmationModal');

    if (event.target === addModal) {
        closeAddItemModal();
    }
    if (event.target === editModal) {
        closeEditItemModal();
    }
    if (event.target === descriptionModal) {
        closeEditDescriptionModal();
    }
    if (event.target === confirmationModal) {
        closeConfirmationModal();
    }
}