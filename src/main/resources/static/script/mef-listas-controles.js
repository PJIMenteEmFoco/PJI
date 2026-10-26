/* Controles das listas integradas, sem dependência do protótipo local. */
(() => {
  'use strict';
  const id = value => document.getElementById(value);
  id('btn-confirm-logout')?.addEventListener('click', () => id('mef-lista-logout')?.requestSubmit());
  const button = id('toggle-delivered-btn'), grid = id('delivered-grid');
  if (button && grid) {
    button.setAttribute('aria-controls', 'delivered-grid');
    button.setAttribute('aria-expanded', String(grid.classList.contains('open')));
    button.addEventListener('click', () => {
      const open = grid.classList.toggle('open');
      button.classList.toggle('open', open);
      button.setAttribute('aria-expanded', String(open));
    });
  }
})();
