/* Mantém a origem da alteração de senha durante as etapas do formulário Thymeleaf. */
(() => {
  'use strict';
  const query = new URLSearchParams(location.search);
  const roleParam = query.get('perfil');
  const allowedRoles = new Set(['aluno', 'professor', 'psicopedagogo']);
  const role = allowedRoles.has(roleParam) ? roleParam : '';
  const fromSettings = query.get('origem') === 'ajustes' && Boolean(role);
  const routes = {
    aluno: '/aluno/configuracoes',
    professor: '/professor/configuracoes',
    psicopedagogo: '/psicopedagogo/configuracoes'
  };
  const settings = fromSettings ? routes[role] : '/login';
  const next = (file) => {
    const target = file.includes('nova-senha') ? '/nova-senha' :
      file.includes('codigo-verificacao') ? '/codigo-verificacao' : '/email-verificacao';
    return target + (fromSettings ? '?origem=ajustes&perfil=' + encodeURIComponent(role) : '');
  };
  window.MEFPasswordFlow = { next, finish: () => { location.href = settings; } };

  document.addEventListener('DOMContentLoaded', () => {
    if (!fromSettings) return;
    document.body.classList.add('password-settings-flow');
    document.querySelectorAll('a[href]').forEach((link) => {
      if (/(?:^|\/)login(?:\.html)?(?:[?#]|$)/.test(link.getAttribute('href') || '')) link.remove();
    });
    const form = document.querySelector('form');
    const submit = form?.querySelector('[type="submit"]');
    if (!form || !submit) return;
    let back = document.querySelector('[data-review-back]');
    if (!back) {
      back = document.createElement('button');
      back.type = 'button';
      back.dataset.reviewBack = 'true';
      back.className = 'review-back';
      back.innerHTML = '<i data-lucide="arrow-left"></i><span>Voltar</span>';
      (document.querySelector('main') || form).prepend(back);
    }
    back.className = submit.className + ' password-settings-return';
    back.textContent = 'Voltar';
    back.onclick = (event) => { event.preventDefault(); location.href = settings; };
    submit.after(back);
  });
})();
