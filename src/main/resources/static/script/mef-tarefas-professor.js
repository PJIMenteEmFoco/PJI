/* Filtra e ordena somente as atividades recebidas do servidor. */
(() => {
  'use strict';
  const grid = document.getElementById('tasks-grid');
  if (!grid) return;
  const cards = [...grid.querySelectorAll('.task-card')];
  const input = document.getElementById('task-search-input');
  const order = document.getElementById('sort-select');
  const buttons = [...document.querySelectorAll('[data-filter]')];
  const empty = document.getElementById('task-no-results');
  if (empty) grid.after(empty);
  const norm = value => String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  let filter = 'all';
  function update() {
    const term = norm(input?.value);
    let visible = 0;
    cards.sort((a, b) => {
      const cmp = (a.dataset.created || '').localeCompare(b.dataset.created || '') || Number(a.dataset.id) - Number(b.dataset.id);
      return order?.value === 'old' ? cmp : -cmp;
    }).forEach(card => {
      const show = norm(card.textContent).includes(term) && (filter !== 'pending' || card.dataset.status !== 'CONCLUIDO');
      card.hidden = !show;
      card.style.display = show ? '' : 'none';
      if (show) visible++;
      grid.append(card);
    });
    if (empty) empty.hidden = visible > 0 || cards.length === 0;
  }
  input?.addEventListener('input', update);
  order?.addEventListener('change', update);
  buttons.forEach(button => button.addEventListener('click', () => {
    filter = button.dataset.filter;
    buttons.forEach(item => { item.classList.toggle('active', item === button); item.setAttribute('aria-pressed', String(item === button)); });
    update();
  }));
  update();
})();
