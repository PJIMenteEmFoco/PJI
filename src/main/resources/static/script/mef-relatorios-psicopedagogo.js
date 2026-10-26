(() => {
  'use strict';

  const $ = (selector, root = document) => root.querySelector(selector);
  const esc = value => String(value ?? '').replace(/[&<>"']/g, char => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[char]));
  const params = new URLSearchParams(window.location.search);
  const taskFilter = params.get('tarefa');
  let reports = [];

  function dateText(value) {
    if (!value) return 'Data não informada';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return 'Data não informada';
    return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(date);
  }

  function dateKey(value) {
    return String(value || '').slice(0, 10);
  }

  function durationText(value) {
    const seconds = Math.max(0, Number(value) || 0);
    const minutes = Math.floor(seconds / 60);
    return `${minutes} min ${seconds % 60} s`;
  }

  function kindText(value) {
    return value === 'manual' ? 'Manual' : 'Automático';
  }

  function typeText(value) {
    const labels = {
      QUIZ: 'Quiz', JOGO_DA_MEMORIA: 'Jogo da memória',
      CRUZADINHA: 'Palavras cruzadas', LEITURA_GUIADA: 'Leitura guiada'
    };
    return labels[value] || value || 'Atividade';
  }

  function showError(message) {
    const element = $('#reports-error');
    if (!element) return;
    element.textContent = message || 'Não foi possível carregar os relatórios.';
    element.hidden = false;
  }

  function hideError() {
    const element = $('#reports-error');
    if (!element) return;
    element.textContent = '';
    element.hidden = true;
  }

  function manualReportContent(report) {
    return `<article class="mef-card">
      <span class="mef-tag">Relatório pedagógico</span>
      <h2>${esc(report.title)}</h2>
      <p class="mef-muted">${esc(report.studentName)} • ${esc(dateText(report.finishedAt))}${report.updatedAt ? ` • Editado em ${esc(dateText(report.updatedAt))}` : ''}</p>
      ${report.author ? `<p class="mef-muted">Responsável: ${esc(report.author)}</p>` : ''}
      <h3>Observações pedagógicas</h3><p class="mef-prose">${esc(report.observations || 'Não informado')}</p>
      <h3>Estratégias utilizadas</h3><p class="mef-prose">${esc(report.strategies || 'Não informado')}</p>
      <h3>Próximos passos</h3><p class="mef-prose">${esc(report.nextSteps || 'Não informado')}</p>
    </article>`;
  }

  function automaticReportContent(report) {
    const stats = [
      ['Acertos', report.correct ?? 0],
      ['Erros', report.errors ?? 0],
      ['Aproveitamento', `${report.score ?? 0}%`],
      ['Tempo de execução', durationText(report.seconds)]
    ];
    return `<article class="mef-card">
      <span class="mef-tag">Resultado automático</span>
      <h2>${esc(report.title)}</h2>
      <p class="mef-muted">${esc(report.studentName)} • ${esc(typeText(report.type))} • Concluída em ${esc(dateText(report.finishedAt))}</p>
      <div class="mef-stats">${stats.map(([label, value]) => `<div class="mef-stat">${esc(label)}<strong>${esc(value)}</strong></div>`).join('')}</div>
      <p class="mef-note">Este relatório apresenta os dados registrados pelo sistema: acertos, total de questões, aproveitamento e duração. As respostas individuais não são armazenadas pelo modelo atual.</p>
    </article>`;
  }

  function openReport(report) {
    const dialog = document.createElement('dialog');
    dialog.className = 'mef-dialog';
    dialog.innerHTML = `<h2>Relatório do aluno</h2>
      ${report.kind === 'manual' ? manualReportContent(report) : automaticReportContent(report)}
      <div class="mef-actions"><button class="mef-btn primary" type="button" data-close>Fechar</button></div>`;
    document.body.append(dialog);
    window.MEFPopups?.prepare(dialog);
    dialog.addEventListener('close', () => dialog.remove(), { once: true });
    dialog.querySelector('[data-close]')?.addEventListener('click', () => dialog.close());
    dialog.showModal();
  }

  function reportCard(report) {
    const automaticSummary = report.kind === 'automatico'
      ? `<p>${esc(report.correct ?? 0)} acertos • ${esc(report.errors ?? 0)} erros • ${esc(report.score ?? 0)}% • ${esc(durationText(report.seconds))}</p>`
      : '';
    const editLink = report.kind === 'manual' && report.sourceId
      ? `<a class="mef-btn" href="/psicopedagogo/relatorios/manuais/${encodeURIComponent(report.sourceId)}/editar">Editar relatório</a>`
      : '';
    return `<article class="mef-card" data-report-card>
      <span class="mef-tag">${esc(kindText(report.kind))}</span>
      <h2>${esc(report.title)}</h2>
      <p>${esc(report.studentName)} • ${esc(dateText(report.finishedAt))}</p>
      ${automaticSummary}
      <div class="mef-actions">
        <button class="mef-btn primary" type="button" data-report="${esc(report.id)}">Visualizar relatório</button>
        ${editLink}
        <a class="mef-btn" href="/psicopedagogo/alunos/${encodeURIComponent(report.studentId)}/ficha">Ficha do aluno</a>
      </div>
    </article>`;
  }

  function currentReports() {
    const query = ($('#search')?.value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
    const studentId = $('#student-filter')?.value || '';
    const kind = $('#kind-filter')?.value || '';
    const day = $('#date-filter')?.value || '';
    return reports.filter(report => {
      if (taskFilter && String(report.taskId ?? '') !== taskFilter) return false;
      if (studentId && String(report.studentId) !== studentId) return false;
      if (kind && report.kind !== kind) return false;
      if (day && dateKey(report.finishedAt) !== day) return false;
      const haystack = `${report.title || ''} ${report.studentName || ''}`
        .normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
      return haystack.includes(query);
    }).sort((a, b) => String(b.finishedAt || '').localeCompare(String(a.finishedAt || '')));
  }

  function render() {
    const list = $('#report-list');
    if (!list) return;
    const filtered = currentReports();
    list.setAttribute('aria-busy', 'false');
    list.innerHTML = filtered.map(reportCard).join('') ||
      '<p class="mef-empty">Nenhum relatório encontrado. Conclua uma atividade ou escreva um relatório manual.</p>';

    list.querySelectorAll('[data-report]').forEach(button => {
      button.addEventListener('click', () => {
        const report = reports.find(item => item.id === button.dataset.report);
        if (report) openReport(report);
      });
    });
    window.lucide?.createIcons();
  }

  async function loadReports() {
    const list = $('#report-list');
    try {
      const response = await fetch('/psicopedagogo/relatorios/dados', {
        method: 'GET', credentials: 'same-origin', headers: { 'Accept': 'application/json' }
      });
      if (!response.ok) {
        if (response.status === 401 || response.status === 403) throw new Error('Sua sessão não tem autorização para consultar estes relatórios.');
        throw new Error('Não foi possível consultar os relatórios no servidor.');
      }
      const data = await response.json();
      reports = Array.isArray(data) ? data : [];
      const studentSelect = $('#student-filter');
      const requestedStudent = params.get('aluno');
      if (studentSelect && requestedStudent && [...studentSelect.options].some(option => option.value === requestedStudent)) {
        studentSelect.value = requestedStudent;
      }
      hideError();
      render();
    } catch (error) {
      if (list) {
        list.setAttribute('aria-busy', 'false');
        list.innerHTML = '<p class="mef-empty">Os relatórios não puderam ser carregados.</p>';
      }
      showError(error instanceof Error ? error.message : 'Não foi possível carregar os relatórios.');
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
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
    refreshToggle();
    const logout = $('#logout-dialog');
    $('#btn-open-logout')?.addEventListener('click', () => logout?.showModal());
    $('#cancelar-logout')?.addEventListener('click', () => logout?.close());

    const writingLink = $('#btn-escrever-relatorio');
    const syncWritingLink = () => {
      if (!writingLink) return;
      const selectedStudent = $('#student-filter')?.value || '';
      writingLink.href = selectedStudent
        ? `/psicopedagogo/relatorios/escrever?aluno=${encodeURIComponent(selectedStudent)}`
        : '/psicopedagogo/relatorios/escrever';
    };
    for (const selector of ['#search', '#student-filter', '#kind-filter', '#date-filter']) {
      const field = $(selector);
      field?.addEventListener(selector === '#search' ? 'input' : 'change', () => {
        render();
        if (selector === '#student-filter') syncWritingLink();
      });
    }
    syncWritingLink();
    window.lucide?.createIcons();
    loadReports();
  });
})();
