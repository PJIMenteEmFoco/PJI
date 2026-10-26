/* Busca/filtro da lista de alunos e controles comuns do perfil psicopedagogo. */
(() => {
  'use strict';
  const normalize = value => String(value ?? '').normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR');

  const search = document.querySelector('#search');
  const selector = document.querySelector('#class-filter');
  const cards = Array.from(document.querySelectorAll('.aluno-card'));
  const count = document.querySelector('#students-count');
  const noData = document.querySelector('#students-no-data');
  const noMatch = document.querySelector('#students-no-match');

  const render = () => {
    const query = normalize(search?.value).trim();
    const turmaId = String(selector?.value ?? '');
    let visible = 0;
    for (const card of cards) {
      const matchesText = normalize(card.dataset.search).includes(query);
      const turmaIds = String(card.dataset.turmas ?? '').split('|').filter(Boolean);
      const matchesTurma = !turmaId || turmaIds.includes(turmaId);
      const matches = matchesText && matchesTurma;
      card.hidden = !matches;
      if (matches) visible++;
    }
    if (count) count.textContent = `${visible} ${visible === 1 ? 'aluno encontrado' : 'alunos encontrados'}`;
    if (noData) noData.hidden = cards.length > 0;
    if (noMatch) noMatch.hidden = cards.length === 0 || visible > 0;
  };
  search?.addEventListener('input', render);
  selector?.addEventListener('change', render);
  render();

  const sidebar = document.querySelector('#sidebar');
  const toggle = document.querySelector('#toggle-btn');
  if (sidebar && toggle) {
    const updateToggle = () => {
      const expanded = sidebar.classList.contains('expanded');
      toggle.setAttribute('aria-expanded', String(expanded));
      toggle.setAttribute('aria-label', expanded ? 'Recolher menu' : 'Expandir menu');
      toggle.title = expanded ? 'Recolher menu' : 'Expandir menu';
      const icon = toggle.querySelector('[data-lucide]');
      if (icon) icon.setAttribute('data-lucide', expanded ? 'chevron-left' : 'chevron-right');
      window.lucide?.createIcons();
      try {
        if (window.matchMedia('(min-width: 651px)').matches) {
          sessionStorage.setItem('mef-menu:psicopedagogo', expanded ? 'expanded' : 'collapsed');
        }
      } catch (_) { /* armazenamento do navegador pode estar bloqueado */ }
    };
    const flip = () => { sidebar.classList.toggle('expanded'); updateToggle(); };
    toggle.addEventListener('click', flip);
    toggle.addEventListener('keydown', event => {
      if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); flip(); }
    });
    updateToggle();
  }

  const logout = document.querySelector('#btn-open-logout');
  const logoutDialog = document.querySelector('#logout-dialog');
  const cancelLogout = document.querySelector('#cancel-logout');
  logout?.addEventListener('click', () => logoutDialog?.showModal());
  cancelLogout?.addEventListener('click', () => logoutDialog?.close());
  logoutDialog?.addEventListener('click', event => {
    if (event.target === logoutDialog) logoutDialog.close();
  });
  window.lucide?.createIcons();
})();
