(() => {
  'use strict';

  const onReady = (callback) => {
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', callback, { once: true });
    else callback();
  };

  onReady(() => {
    if (window.lucide && typeof window.lucide.createIcons === 'function') {
      window.lucide.createIcons();
    }

    const sidebar = document.getElementById('sidebar');
    const toggle = document.getElementById('toggle-btn');
    const logoutModal = document.getElementById('logout-modal');
    const logoutOpen = document.getElementById('btn-open-logout');
    const logoutCancel = document.getElementById('btn-cancel-logout');
    const logoutConfirm = document.getElementById('btn-confirm-logout');
    const logoutForm = document.getElementById('logout-form');

    const menuStorageKey = 'mef-menu:psicopedagogo';
    const persistMenu = () => {
      if (!sidebar) return;
      try {
        sessionStorage.setItem(menuStorageKey, sidebar.classList.contains('expanded') ? 'expanded' : 'collapsed');
      } catch (_) { /* A interface continua funcionando sem sessionStorage. */ }
    };

    const toggleMenu = () => {
      if (!sidebar) return;
      sidebar.classList.toggle('expanded');
      persistMenu();
    };
    toggle?.addEventListener('click', toggleMenu);
    toggle?.addEventListener('keydown', (event) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        toggleMenu();
      }
    });

    const showLogout = () => {
      if (!logoutModal) return;
      logoutModal.classList.add('active');
      logoutModal.setAttribute('aria-hidden', 'false');
      logoutCancel?.focus();
    };
    const hideLogout = () => {
      logoutModal?.classList.remove('active');
      logoutModal?.setAttribute('aria-hidden', 'true');
      logoutOpen?.focus();
    };
    logoutOpen?.addEventListener('click', showLogout);
    logoutCancel?.addEventListener('click', hideLogout);
    logoutConfirm?.addEventListener('click', () => logoutForm?.requestSubmit());
    logoutModal?.addEventListener('click', (event) => {
      if (event.target === logoutModal) hideLogout();
    });
    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape' && logoutModal?.classList.contains('active')) hideLogout();
    });

    const search = document.getElementById('task-search');
    const cards = Array.from(document.querySelectorAll('[data-task-card="true"]'));
    const filterButtons = Array.from(document.querySelectorAll('[data-filter]'));
    const filterEmpty = document.getElementById('no-filter-results');
    const initialEmpty = document.getElementById('empty-data');
    let activeFilter = 'all';

    const normalize = (value) => String(value || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLocaleLowerCase('pt-BR')
      .trim();

    const applyFilters = () => {
      const query = normalize(search?.value);
      let visible = 0;
      cards.forEach((card) => {
        const text = normalize(`${card.dataset.titulo || ''} ${card.dataset.aluno || ''} ${card.dataset.turma || ''}`);
        const matchesSearch = !query || text.includes(query);
        const matchesFilter = activeFilter === 'all' ||
          (activeFilter === 'pending-reading' && card.dataset.pendenteLeitura === 'true');
        const show = matchesSearch && matchesFilter;
        card.hidden = !show;
        if (show) visible += 1;
      });
      if (filterEmpty) filterEmpty.hidden = cards.length === 0 || visible > 0;
      if (initialEmpty) initialEmpty.hidden = query.length > 0 || activeFilter !== 'all';
    };

    search?.addEventListener('input', applyFilters);
    filterButtons.forEach((button) => {
      button.addEventListener('click', () => {
        activeFilter = button.dataset.filter || 'all';
        filterButtons.forEach((item) => {
          const active = item === button;
          item.classList.toggle('active', active);
          item.setAttribute('aria-pressed', String(active));
        });
        applyFilters();
      });
    });
    applyFilters();
  });
})();
