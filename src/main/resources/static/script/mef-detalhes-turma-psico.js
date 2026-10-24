(() => {
    'use strict';

    const byId = (id) => document.getElementById(id);

    document.addEventListener('DOMContentLoaded', () => {
        const sidebar = byId('sidebar');
        const toggle = byId('toggle-btn');
        const search = byId('search-student');
        const grid = byId('students-grid');
        const noSearchResults = byId('students-empty-search');
        const logoutModal = byId('logout-modal');
        const openLogout = byId('btn-open-logout');
        const cancelLogout = byId('btn-cancel-logout');

        const createIcons = () => {
            if (window.lucide && typeof window.lucide.createIcons === 'function') {
                window.lucide.createIcons();
            }
        };
        createIcons();

        const setSidebarExpanded = (expanded) => {
            if (!sidebar || !toggle) return;
            sidebar.classList.toggle('expanded', expanded);
            toggle.setAttribute('aria-expanded', String(expanded));
        };

        toggle?.addEventListener('click', () => {
            setSidebarExpanded(!sidebar?.classList.contains('expanded'));
        });
        toggle?.addEventListener('keydown', (event) => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                toggle.click();
            }
        });

        const normalize = (value) => String(value ?? '')
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLocaleLowerCase('pt-BR')
            .trim();

        const filterStudents = () => {
            if (!grid || !search) return;
            const query = normalize(search.value);
            const cards = Array.from(grid.querySelectorAll('.student-card'));
            let visible = 0;

            cards.forEach((card) => {
                const text = normalize(`${card.dataset.nome || ''} ${card.dataset.email || ''}`);
                const matches = text.includes(query);
                card.style.display = matches ? '' : 'none';
                if (matches) visible += 1;
            });

            if (noSearchResults) {
                noSearchResults.style.display = cards.length > 0 && visible === 0 ? 'flex' : 'none';
            }
        };
        search?.addEventListener('input', filterStudents);
        filterStudents();

        const setLogoutOpen = (open) => {
            if (!logoutModal) return;
            logoutModal.classList.toggle('active', open);
            logoutModal.setAttribute('aria-hidden', String(!open));
        };
        openLogout?.addEventListener('click', () => setLogoutOpen(true));
        cancelLogout?.addEventListener('click', () => setLogoutOpen(false));
        logoutModal?.addEventListener('click', (event) => {
            if (event.target === logoutModal) setLogoutOpen(false);
        });
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') setLogoutOpen(false);
        });
    });
})();
