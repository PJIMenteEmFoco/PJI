/* Lista real de alunos do professor. Não utiliza o banco demonstrativo do front. */
(() => {
  'use strict';
  const search = document.querySelector('#search');
  const selector = document.querySelector('#class-filter');
  const cards = Array.from(document.querySelectorAll('.aluno-card'));
  const count = document.querySelector('#students-count');
  const noMatch = document.querySelector('#students-no-match');
  const normalize = value => String(value || '').normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR');

  const render = () => {
    const query = normalize(search?.value).trim();
    const turmaId = String(selector?.value || '');
    let visible = 0;
    for (const card of cards) {
      const matchesText = normalize(card.dataset.search).includes(query);
      const matchesTurma = !turmaId || (card.dataset.turmas || '').split('|').includes(turmaId);
      const display = matchesText && matchesTurma;
      card.hidden = !display;
      if (display) visible++;
    }
    if (count) count.textContent = `${visible} ${visible === 1 ? 'aluno encontrado' : 'alunos encontrados'}`;
    if (noMatch) noMatch.hidden = cards.length === 0 || visible !== 0;
  };
  search?.addEventListener('input', render);
  selector?.addEventListener('change', render);
  render();

  const sidebar = document.querySelector('#sidebar');
  const toggle = document.querySelector('#toggle-btn');
  if (toggle && sidebar) {
    toggle.setAttribute('role', 'button');
    toggle.setAttribute('tabindex', '0');
    toggle.setAttribute('aria-label', 'Expandir ou recolher menu');
    const flip = () => sidebar.classList.toggle('expanded');
    toggle.addEventListener('click', flip);
    toggle.addEventListener('keydown', e => {
      if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); flip(); }
    });
  }
  const logout = document.querySelector('#btn-open-logout');
  logout?.addEventListener('click', () => {
    if (!window.confirm('Tem certeza que deseja sair da sua conta?')) return;
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/logout';
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const name = document.querySelector('meta[name="_csrf_parameter"]')?.content;
    if (token && name) {
      const input = document.createElement('input');
      input.type = 'hidden'; input.name = name; input.value = token;
      form.appendChild(input);
    }
    document.body.appendChild(form);
    form.submit();
  });
  if (window.lucide?.createIcons) window.lucide.createIcons();
})();
