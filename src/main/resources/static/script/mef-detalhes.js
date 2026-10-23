(() => {
    'use strict';

    const byId = (id) => document.getElementById(id);
    const sidebar = byId('sidebar');
    const toggle = byId('toggle-btn');
    const logoutModal = byId('logout-modal');
    const inviteModal = byId('vincular-aluno-modal');

    if (window.lucide && typeof window.lucide.createIcons === 'function') {
        window.lucide.createIcons();
    }

    toggle?.addEventListener('click', () => sidebar?.classList.toggle('expanded'));

    const search = byId('search-student');
    const grid = byId('students-grid');
    const noSearchResults = byId('students-empty-search');
    search?.addEventListener('input', () => {
        const query = search.value.trim().toLocaleLowerCase('pt-BR');
        const cards = Array.from(grid?.querySelectorAll('.student-card') || []);
        let visible = 0;
        cards.forEach((card) => {
            const text = `${card.dataset.nome || ''} ${card.dataset.email || ''}`.toLocaleLowerCase('pt-BR');
            const matches = text.includes(query);
            card.style.display = matches ? '' : 'none';
            if (matches) visible++;
        });
        if (noSearchResults) noSearchResults.hidden = !(cards.length > 0 && visible === 0);
    });

    const openModal = (modal) => {
        if (!modal) return;
        modal.classList.add('active');
        modal.setAttribute('aria-hidden', 'false');
    };
    const closeModal = (modal) => {
        if (!modal) return;
        modal.classList.remove('active');
        modal.setAttribute('aria-hidden', 'true');
    };

    byId('btn-open-vincular-aluno')?.addEventListener('click', () => {
        openModal(inviteModal);
        byId('email-integrante')?.focus();
    });
    byId('btn-cancel-vincular')?.addEventListener('click', () => closeModal(inviteModal));
    byId('btn-open-logout')?.addEventListener('click', () => openModal(logoutModal));
    byId('btn-cancel-logout')?.addEventListener('click', () => closeModal(logoutModal));
    byId('btn-confirm-logout')?.addEventListener('click', () => byId('logoutForm')?.submit());

    [inviteModal, logoutModal].filter(Boolean).forEach((modal) => {
        modal.addEventListener('click', (event) => {
            if (event.target === modal) closeModal(modal);
        });
    });
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            closeModal(inviteModal);
            closeModal(logoutModal);
        }
    });
})();
