const sidebar = document.getElementById('sidebar');
const menuToggle = document.getElementById('menuToggle');
const menuLabel = menuToggle.querySelector('.menu-toggle-label');

menuToggle.addEventListener('click', () => {
    const isExpanded = sidebar.classList.toggle('expanded');
    menuToggle.setAttribute('aria-expanded', String(isExpanded));
    menuLabel.textContent = isExpanded ? 'Fechar Menu' : 'Abrir Menu';
});

