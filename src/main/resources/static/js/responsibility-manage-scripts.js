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
    const pattern = /^[A-Za-z0-9 ]+$/;
    if (!pattern.test(itemName)) {
        return "Item name can only contain letters, numbers, and spaces";
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

// Delete Item Modal Functions
function showDeleteItemModal(itemId, itemName) {
    document.getElementById('deleteItemId').value = itemId;
    document.getElementById('deleteItemMessage').textContent =
        'Are you sure you want to delete "' + itemName + '"? This action cannot be undone.';
    document.getElementById('deleteItemModal').style.display = 'block';
}

function closeDeleteItemModal() {
    document.getElementById('deleteItemModal').style.display = 'none';
}

function confirmDeleteItem() {
    document.getElementById('deleteItemForm').submit();
}

// Window click handler for modal backgrounds
window.onclick = function(event) {
    const addModal = document.getElementById('addItemModal');
    const editModal = document.getElementById('editItemModal');
    const deleteModal = document.getElementById('deleteItemModal');
    const descriptionModal = document.getElementById('editDescriptionModal');

    if (event.target === addModal) {
        closeAddItemModal();
    }
    if (event.target === editModal) {
        closeEditItemModal();
    }
    if (event.target === deleteModal) {
        closeDeleteItemModal();
    }
    if (event.target === descriptionModal) {
        closeEditDescriptionModal();
    }
}