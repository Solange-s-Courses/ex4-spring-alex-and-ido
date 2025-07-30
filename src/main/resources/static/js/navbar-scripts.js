function toggleMobileMenu() {
    const mobileMenu = document.getElementById('mobileMenu');
    const hamburger = document.querySelector('.hamburger');

    // Toggle mobile menu
    mobileMenu.classList.toggle('show');

    // Toggle hamburger animation
    hamburger.classList.toggle('active');

    // Prevent body scroll when menu is open (optional)
    if (mobileMenu.classList.contains('show')) {
        document.body.classList.add('mobile-menu-open');
    } else {
        document.body.classList.remove('mobile-menu-open');
    }
}

// Close mobile menu when clicking outside (optional)
document.addEventListener('click', function(event) {
    const navbar = document.querySelector('.navbar');
    const mobileMenu = document.getElementById('mobileMenu');

    if (mobileMenu && mobileMenu.classList.contains('show') && !navbar.contains(event.target)) {
        toggleMobileMenu();
    }
});

// Close mobile menu when window is resized to desktop size
window.addEventListener('resize', function() {
    const mobileMenu = document.getElementById('mobileMenu');
    const hamburger = document.querySelector('.hamburger');

    if (window.innerWidth > 768 && mobileMenu.classList.contains('show')) {
        mobileMenu.classList.remove('show');
        hamburger.classList.remove('active');
        document.body.classList.remove('mobile-menu-open');
    }
});