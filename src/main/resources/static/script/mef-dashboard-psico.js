/* Interações específicas do dashboard do psicopedagogo, sem dados demonstrativos. */
(() => {
  'use strict';

  const sidebar = document.getElementById('sidebar');
  const toggle = document.getElementById('toggle-btn');
  const modal = document.getElementById('logout-modal');
  const openLogout = document.getElementById('btn-open-logout');
  const cancelLogout = document.getElementById('btn-cancel-logout');

  const setModal = (open) => {
    if (!modal) return;
    modal.classList.toggle('active', open);
    modal.setAttribute('aria-hidden', String(!open));
    if (open) cancelLogout?.focus();
    else openLogout?.focus();
  };

  if (toggle && sidebar) {
    const flip = () => {
      const expanded = sidebar.classList.toggle('expanded');
      toggle.setAttribute('aria-expanded', String(expanded));
      try {
        sessionStorage.setItem('mef-menu:psicopedagogo', expanded ? 'expanded' : 'collapsed');
      } catch (_) { /* O menu continua funcional se sessionStorage estiver indisponível. */ }
    };
    toggle.setAttribute('role', 'button');
    toggle.setAttribute('tabindex', '0');
    toggle.setAttribute('aria-expanded', String(sidebar.classList.contains('expanded')));
    toggle.addEventListener('click', flip);
    toggle.addEventListener('keydown', (event) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        flip();
      }
    });
  }

  openLogout?.addEventListener('click', () => setModal(true));
  cancelLogout?.addEventListener('click', () => setModal(false));
  modal?.addEventListener('click', (event) => {
    if (event.target === modal) setModal(false);
  });
  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && modal?.classList.contains('active')) setModal(false);
  });

  window.lucide?.createIcons();
})();
