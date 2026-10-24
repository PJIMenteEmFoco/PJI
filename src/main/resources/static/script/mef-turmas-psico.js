(() => {
    'use strict';

    const byId = (id) => document.getElementById(id);
    const normalize = (value) => String(value ?? '')
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLocaleLowerCase('pt-BR')
        .trim();

    document.addEventListener('DOMContentLoaded', () => {
        if (window.lucide && typeof window.lucide.createIcons === 'function') {
            window.lucide.createIcons();
        }

        const sidebar = byId('sidebar');
        const toggle = byId('toggle-btn');
        if (sidebar && toggle) {
            const toggleSidebar = () => sidebar.classList.toggle('expanded');
            toggle.addEventListener('click', toggleSidebar);
            toggle.addEventListener('keydown', (event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    toggleSidebar();
                }
            });
        }

        const grid = byId('turmas-grid');
        const search = byId('input-search-turmas');
        const filter = byId('select-filter-psico');
        const filterEmpty = byId('turmas-filtro-vazio');
        const cards = grid ? Array.from(grid.querySelectorAll('.turma-card')) : [];

        const filterCards = () => {
            if (!search || !filter || !grid) return;
            const term = normalize(search.value);
            const selectedPsychopedagogue = filter.value;
            let visibleCards = 0;

            cards.forEach((card) => {
                const searchData = normalize(card.dataset.search || card.textContent);
                const psychopedagogues = String(card.dataset.psicoIds || '')
                    .split('|').filter(Boolean);
                const matchesText = !term || searchData.includes(term);
                const matchesPsychopedagogue = selectedPsychopedagogue === 'todos'
                    || psychopedagogues.includes(selectedPsychopedagogue);
                const visible = matchesText && matchesPsychopedagogue;
                card.style.display = visible ? '' : 'none';
                if (visible) visibleCards += 1;
            });

            if (filterEmpty) {
                filterEmpty.hidden = cards.length === 0 || visibleCards > 0;
            }
        };

        if (search) search.addEventListener('input', filterCards);
        if (filter) filter.addEventListener('change', filterCards);

        const modal = byId('logout-modal');
        const openLogout = byId('btn-open-logout');
        const cancelLogout = byId('btn-cancel-logout');
        const setModal = (open) => {
            if (!modal) return;
            modal.classList.toggle('active', open);
            modal.setAttribute('aria-hidden', String(!open));
        };

        if (openLogout) openLogout.addEventListener('click', () => setModal(true));
        if (cancelLogout) cancelLogout.addEventListener('click', () => setModal(false));
        if (modal) {
            modal.addEventListener('click', (event) => {
                if (event.target === modal) setModal(false);
            });
        }
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && modal && modal.classList.contains('active')) {
                setModal(false);
            }
        });
        filterCards();
    });
})();
