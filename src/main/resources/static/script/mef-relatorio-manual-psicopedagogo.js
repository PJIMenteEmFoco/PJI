(() => {
  'use strict';
  const $ = (selector, root = document) => root.querySelector(selector);
  const esc = value => String(value ?? '').replace(/[&<>"']/g, char => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[char]));
  const root = $('#mef-root');
  if (!root) return;
  const mode = root.dataset.mode || 'criar';
  const editing = mode === 'editar';
  const reportId = root.dataset.reportId || '';
  const initialStudentId = root.dataset.studentId || new URLSearchParams(location.search).get('aluno') || '';
  const sourceStudents = $('#report-students-source');
  const students = sourceStudents ? [...sourceStudents.options].map(option => ({ id: option.value, name: option.textContent })) : [];
  let originalReport = null;

  const link = (href, label, primary = false) => `<a class="mef-btn${primary ? ' primary' : ''}" href="${esc(href)}">${esc(label)}</a>`;
  const field = (id, label, value = '', required = false, maxLength = 20000) =>
    `<label class="mef-field" for="${id}">${esc(label)}<textarea id="${id}" name="${id}" ${required ? 'required' : ''} maxlength="${maxLength}">${esc(value)}</textarea></label>`;
  const errorBox = message => {
    const box = $('#manual-report-error');
    if (box) { box.textContent = message || ''; box.hidden = !message; }
  };
  const formValues = () => ({
    alunoId: Number($('#report-student').value),
    titulo: $('#report-title').value.trim(),
    observacoes: $('#observations').value.trim(),
    estrategias: $('#strategies').value.trim(),
    proximosPassos: $('#next-steps').value.trim()
  });

  function render(report = null) {
    originalReport = report;
    const selectedStudentId = String(report?.alunoId ?? initialStudentId ?? '');
    const studentOptions = students.map(student =>
      `<option value="${esc(student.id)}" ${String(student.id) === selectedStudentId ? 'selected' : ''}>${esc(student.name)}</option>`
    ).join('');
    root.innerHTML = `
      <header class="mef-header">
        <div>
          <h1>${editing ? 'Editar relatório manual' : 'Escrever relatório manual'}</h1>
          <p class="mef-sub">Registre observações pedagógicas e estratégias de acompanhamento.</p>
        </div>
        <div class="mef-actions">${link('/psicopedagogo/relatorios', 'Voltar aos relatórios')}</div>
      </header>
      <form id="manual-report-form" class="mef-card" novalidate>
        <p class="mef-error" id="manual-report-error" role="alert" hidden></p>
        <label class="mef-field" for="report-student">Aluno
          <select id="report-student" name="alunoId" required>
            <option value="">Selecione um aluno</option>${studentOptions}
          </select>
        </label>
        <label class="mef-field" for="report-title">Título
          <input id="report-title" name="titulo" type="text" value="${esc(report?.titulo || '')}" maxlength="160" required />
        </label>
        ${field('observations', 'Observações pedagógicas', report?.observacoes || '', true)}
        ${field('strategies', 'Estratégias utilizadas', report?.estrategias || '', false)}
        ${field('next-steps', 'Próximos passos', report?.proximosPassos || '', false)}
        <div class="mef-actions">
          ${link('/psicopedagogo/relatorios', 'Cancelar')}
          <button class="mef-btn primary" id="save-manual-report" type="submit">${editing ? 'Salvar alterações' : 'Salvar relatório'}</button>
        </div>
      </form>`;
    $('#manual-report-form').addEventListener('submit', save);
    window.lucide?.createIcons();
  }

  async function loadForEdit() {
    if (!reportId || !/^\d+$/.test(reportId)) {
      throw new Error('O identificador do relatório é inválido.');
    }
    const response = await fetch(`/psicopedagogo/relatorios/manuais/${encodeURIComponent(reportId)}/dados`, {
      credentials: 'same-origin', headers: { 'Accept': 'application/json' }
    });
    if (!response.ok) throw new Error(await responseMessage(response, 'Não foi possível carregar o relatório para edição.'));
    return response.json();
  }

  async function responseMessage(response, fallback) {
    try {
      const body = await response.json();
      return body.detail || body.message || body.error || fallback;
    } catch (_) { return fallback; }
  }

  async function save(event) {
    event.preventDefault();
    errorBox('');
    const values = formValues();
    if (!values.alunoId || !values.titulo || !values.observacoes) {
      errorBox('Selecione o aluno e preencha o título e as observações pedagógicas.');
      return;
    }
    const button = $('#save-manual-report');
    const oldText = button.textContent;
    button.disabled = true;
    button.textContent = 'Salvando...';
    const csrf = $('meta[name="_csrf"]')?.content;
    const csrfHeader = $('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    const headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
    if (csrf && csrfHeader) headers[csrfHeader] = csrf;
    const url = editing
      ? `/psicopedagogo/relatorios/manuais/${encodeURIComponent(reportId)}`
      : '/psicopedagogo/relatorios/manuais';
    try {
      const response = await fetch(url, {
        method: editing ? 'PUT' : 'POST',
        credentials: 'same-origin',
        headers,
        body: JSON.stringify(values)
      });
      if (!response.ok) throw new Error(await responseMessage(response, 'Não foi possível salvar o relatório.'));
      const saved = await response.json();
      location.href = `/psicopedagogo/relatorios?aluno=${encodeURIComponent(saved.alunoId ?? values.alunoId)}`;
    } catch (error) {
      errorBox(error instanceof Error ? error.message : 'Não foi possível salvar o relatório.');
      button.disabled = false;
      button.textContent = oldText;
    }
  }

  document.addEventListener('DOMContentLoaded', async () => {
    const sidebar = $('#sidebar');
    const toggle = $('#toggle-btn');
    const refreshToggle = () => {
      const expanded = sidebar?.classList.contains('expanded') === true;
      if (toggle) {
        toggle.setAttribute('role', 'button');
        toggle.setAttribute('tabindex', '0');
        toggle.setAttribute('aria-expanded', String(expanded));
        toggle.setAttribute('aria-controls', 'sidebar');
        toggle.innerHTML = `<i data-lucide="chevron-${expanded ? 'left' : 'right'}"></i>`;
        window.lucide?.createIcons();
      }
    };
    toggle?.addEventListener('click', () => { sidebar?.classList.toggle('expanded'); refreshToggle(); });
    toggle?.addEventListener('keydown', event => {
      if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); toggle.click(); }
    });
    $('#btn-open-logout')?.addEventListener('click', () => $('#logout-dialog')?.showModal());
    $('#cancelar-logout')?.addEventListener('click', () => $('#logout-dialog')?.close());
    refreshToggle();
    window.lucide?.createIcons();
    try {
      if (editing) render(await loadForEdit());
      else render();
    } catch (error) {
      root.innerHTML = `<header class="mef-header"><div><h1>Relatório indisponível</h1><p class="mef-sub">${esc(error instanceof Error ? error.message : 'Não foi possível abrir este relatório.')}</p></div><div class="mef-actions">${link('/psicopedagogo/relatorios', 'Voltar aos relatórios', true)}</div></header>`;
      window.lucide?.createIcons();
    }
  });
})();
