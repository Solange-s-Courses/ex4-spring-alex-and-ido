// User Info Page Scripts - Modern Modal Management

// Modal Management
function openNameModal() {
    const modal = document.getElementById('nameModal');
    if (modal) {
        modal.classList.add('show');
        // Focus on first input
        setTimeout(() => {
            const firstInput = modal.querySelector('input[name="firstName"]');
            if (firstInput) {
                firstInput.focus();
            }
        }, 100);
    }
}

function closeNameModal() {
    const modal = document.getElementById('nameModal');
    if (modal) {
        modal.classList.remove('show');
    }
}

function openPhoneModal() {
    const modal = document.getElementById('phoneModal');
    if (modal) {
        modal.classList.add('show');
        // Focus on phone input
        setTimeout(() => {
            const phoneInput = modal.querySelector('input[name="phoneNumber"]');
            if (phoneInput) {
                phoneInput.focus();
            }
        }, 100);
    }
}

function closePhoneModal() {
    const modal = document.getElementById('phoneModal');
    if (modal) {
        modal.classList.remove('show');
    }
}

// Initialize event listeners when DOM is ready
document.addEventListener('DOMContentLoaded', function() {

    // Close modals when clicking outside
    const nameModal = document.getElementById('nameModal');
    const phoneModal = document.getElementById('phoneModal');

    if (nameModal) {
        nameModal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeNameModal();
            }
        });
    }

    if (phoneModal) {
        phoneModal.addEventListener('click', function(e) {
            if (e.target === this) {
                closePhoneModal();
            }
        });
    }

    // Handle ESC key to close modals
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            if (nameModal && nameModal.classList.contains('show')) {
                closeNameModal();
            }
            if (phoneModal && phoneModal.classList.contains('show')) {
                closePhoneModal();
            }
        }
    });

    // Form validation for name modal
    const nameForm = document.querySelector('#nameModal form');
    if (nameForm) {
        nameForm.addEventListener('submit', function(e) {
            const firstName = this.querySelector('input[name="firstName"]').value.trim();
            const lastName = this.querySelector('input[name="lastName"]').value.trim();

            // Validate names (only letters, 1-20 characters)
            const namePattern = /^[A-Za-z]{1,20}$/;

            if (!namePattern.test(firstName)) {
                e.preventDefault();
                Toast.error('First name must contain only letters and be 1-20 characters long');
                return;
            }

            if (!namePattern.test(lastName)) {
                e.preventDefault();
                Toast.error('Last name must contain only letters and be 1-20 characters long');
                return;
            }
        });
    }

    // Form validation for phone modal
    const phoneForm = document.querySelector('#phoneModal form');
    if (phoneForm) {
        phoneForm.addEventListener('submit', function(e) {
            const phoneNumber = this.querySelector('input[name="phoneNumber"]').value.trim();

            // Validate phone number (exactly 10 digits)
            const phonePattern = /^[0-9]{10}$/;

            if (!phonePattern.test(phoneNumber)) {
                e.preventDefault();
                Toast.error('Phone number must be exactly 10 digits');
                return;
            }
        });
    }

    // Toast notifications for URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const successMsg = urlParams.get('success');
    const errorMsg = urlParams.get('error');

    if (successMsg) {
        Toast.success(decodeURIComponent(successMsg));
    }
    if (errorMsg) {
        Toast.error(decodeURIComponent(errorMsg));
    }

    // Add smooth animations to info sections
    const infoSections = document.querySelectorAll('.info-section');
    infoSections.forEach((section, index) => {
        // Stagger animation entrance
        setTimeout(() => {
            section.style.opacity = '0';
            section.style.transform = 'translateY(20px)';
            section.style.transition = 'all 0.5s ease';

            // Trigger animation
            requestAnimationFrame(() => {
                section.style.opacity = '1';
                section.style.transform = 'translateY(0)';
            });
        }, index * 100);
    });

    // Add ripple effect to buttons
    addRippleEffectToButtons();
});

// Utility function to add ripple effect to buttons
function addRippleEffectToButtons() {
    const buttons = document.querySelectorAll('.change-btn, .submit-btn, .cancel-btn');

    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
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
            ripple.style.position = 'absolute';
            ripple.style.background = 'rgba(255, 255, 255, 0.6)';
            ripple.style.borderRadius = '50%';
            ripple.style.transform = 'scale(0)';
            ripple.style.animation = 'ripple-animation 0.6s linear';
            ripple.style.pointerEvents = 'none';

            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
}

// Add CSS for ripple animation
const style = document.createElement('style');
style.textContent = `
    @keyframes ripple-animation {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
    
    .change-btn, .submit-btn, .cancel-btn {
        position: relative;
        overflow: hidden;
    }
`;
document.head.appendChild(style);