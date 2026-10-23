/* Notificações persistidas no backend. Estrutura e classes visuais baseadas no PJI-main. */
(() => {
  'use strict';

  const $ = (selector, scope = document) => scope.querySelector(selector);
  const root = $('[data-mef-notifications-host]') || $('#mef-root');
  const externalNavigation = document.currentScript?.dataset.navigation === 'external';
  if (!root) return;
  const find = selector => $(selector, root);
  root.classList.add('mef-notification-surface');

  const apiBase = '/api/notificacoes';
  const csrfToken = $('meta[name="_csrf"]')?.content || '';
  const csrfHeader = $('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
  let state = { notificacoes: [], receberNotificacoes: true, naoLidas: 0 };

  const esc = value => String(value ?? '').replace(/[&<>"']/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[character]));

  const formatDate = value => {
    if (!value) return 'Data não informada';
    const normalized = String(value).includes('T') ? value : `${value}T12:00:00`;
    const date = new Date(normalized);
    return Number.isNaN(date.getTime()) ? 'Data não informada' : date.toLocaleString('pt-BR', {
      dateStyle: 'short', timeStyle: 'short'
    });
  };

  const button = (id, text, type = 'button') =>
    `<button type="${type}" class="mef-btn" id="${id}">${esc(text)}</button>`;

  const header = (title, description, actions = '') =>
    `<header class="mef-header"><div><h1>${esc(title)}</h1><p class="mef-sub">${esc(description)}</p></div><div class="mef-actions">${actions}</div></header>`;

  function toast(text) {
    $('.mef-toast')?.remove();
    const element = document.createElement('div');
    element.className = 'mef-toast mef-notification-toast';
    element.setAttribute('role', 'status');
    element.textContent = text;
    (root.closest('dialog') || document.body).append(element);
    window.setTimeout(() => element.remove(), 3600);
  }

  function showError(text) {
    let element = $('.mef-error', root);
    if (!element) {
      element = document.createElement('p');
      element.className = 'mef-error';
      element.setAttribute('role', 'alert');
      root.prepend(element);
    }
    element.textContent = text;
  }

  function clearError() {
    $('.mef-error', root)?.remove();
  }

  async function request(path, options = {}) {
    const headers = { Accept: 'application/json', ...(options.headers || {}) };
    if (options.body != null) headers['Content-Type'] = 'application/json';
    if (options.method && !['GET', 'HEAD', 'OPTIONS'].includes(options.method.toUpperCase()) && csrfToken) {
      headers[csrfHeader] = csrfToken;
    }
    const response = await fetch(path, {
      credentials: 'same-origin',
      ...options,
      headers
    });
    const contentType = response.headers.get('content-type') || '';
    const body = contentType.includes('application/json') ? await response.json() : null;
    if (!response.ok) {
      throw new Error(body?.detail || body?.mensagem || body?.message || `Não foi possível concluir a operação (HTTP ${response.status}).`);
    }
    return body;
  }

  async function reload() {
    state = await request(apiBase);
    if (!Array.isArray(state.notificacoes)) state.notificacoes = [];
    render();
  }

  function safeInternalLink(value) {
    const profile = ['aluno','professor','psicopedagogo'].includes(document.body.dataset.mefProfile) ? document.body.dataset.mefProfile : 'professor';
    const fallback = '/' + profile + '/dashboard';
    const link = String(value || fallback);
    return link.startsWith('/') && !link.startsWith('//') ? link : fallback;
  }

  function render() {
    root.innerHTML = header('Notificações','Acompanhe as novidades sobre suas atividades e relatórios.',button('mark-all','Marcar todas como lidas')) + `
      <div class="mef-notification-summary" id="notification-summary"></div>
      <section class="mef-notification-settings" aria-label="Preferências de notificações">
        <label><input id="notifications-enabled" type="checkbox" ${state.receberNotificacoes ? 'checked' : ''}>Receber novas notificações</label>
        <p>Os avisos já recebidos continuam disponíveis.</p>
      </section>
      <section class="mef-notification-filters" aria-label="Filtros de notificações">
        <label for="notification-search">Buscar notificação<input id="notification-search" type="search" placeholder="Buscar por título ou mensagem" autocomplete="off"></label>
        <label for="read-filter">Exibir<select id="read-filter"><option value="">Todas</option><option value="unread">Não lidas</option><option value="read">Lidas</option></select></label>
      </section>
      <p id="notification-count" aria-live="polite"></p><div id="notification-list"></div>`;
    find('#mark-all').addEventListener('click',markAllRead);
    find('#read-filter').addEventListener('change',renderList);
    find('#notification-search').addEventListener('input',renderList);
    find('#notifications-enabled').addEventListener('change',changePreference);
    renderList();
  }

  function renderList() {
    const filter=find('#read-filter')?.value||'',query=(find('#notification-search')?.value||'').normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase().trim();
    const list=state.notificacoes.filter(item=>(!filter||(filter==='read'?item.lida:!item.lida))&&String(item.titulo+' '+item.mensagem).normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase().includes(query));
    const host=find('#notification-list');if(!host)return;
    host.innerHTML=list.map(item=>`
      <article class="mef-notification-item ${item.lida?'':'mef-unread'}">
        <span class="mef-tag">${item.lida?'Lida':'Não lida'}</span>
        <h2>${esc(item.titulo)}</h2><p>${esc(item.mensagem)}</p>
        <p class="mef-muted"><time datetime="${esc(item.dataCriacao||'')}">${esc(formatDate(item.dataCriacao))}</time></p>
        <div class="mef-actions"><a class="mef-btn primary" href="${esc(safeInternalLink(item.link))}" data-open-notification="${esc(item.id)}">Abrir</a>
        ${item.lida?'':`<button type="button" class="mef-btn" data-read="${esc(item.id)}">Marcar como lida</button>`}</div>
      </article>`).join('')||`<div class="mef-notification-empty"><h2>${state.notificacoes.length?'Nenhuma notificação nesta seleção':'Tudo em dia!'}</h2><p>${state.notificacoes.length?'Experimente outro termo ou uma opção diferente no filtro.':'Quando houver novidades para você, elas aparecerão aqui.'}</p></div>`;
    host.querySelectorAll('[data-read]').forEach(element=>element.addEventListener('click',async()=>{element.disabled=true;try{await markOneRead(element.dataset.read);}finally{if(element.isConnected)element.disabled=false;}}));
    host.querySelectorAll('[data-open-notification]').forEach(element=>element.addEventListener('click',async event=>{
      event.preventDefault();const destination=safeInternalLink(element.getAttribute('href'));
      try{const item=state.notificacoes.find(value=>String(value.id)===element.dataset.openNotification);if(item&&!item.lida)await request(`${apiBase}/${encodeURIComponent(item.id)}/lida`,{method:'PATCH'});window.location.assign(destination);}
      catch(error){showError(error.message||'Não foi possível marcar a notificação como lida.');}
    }));
    const unread=state.notificacoes.filter(item=>!item.lida).length;
    find('#notification-count').textContent=`${list.length} ${list.length===1?'notificação encontrada':'notificações encontradas'}`;
    find('#notification-summary').innerHTML=[['Notificações',state.notificacoes.length],['Não lidas',unread],['Lidas',state.notificacoes.length-unread]].map(([label,value])=>`<div class="mef-notification-stat"><span>${label}</span><strong>${value}</strong></div>`).join('');
    find('#mark-all').disabled=!unread;
  }

  async function markOneRead(id) {
    clearError();
    try {
      await request(`${apiBase}/${encodeURIComponent(id)}/lida`, { method: 'PATCH' });
      const item = state.notificacoes.find(value => String(value.id) === String(id));
      if (item) item.lida = true;
      renderList();
      toast('Notificação marcada como lida.');
    } catch (error) {
      showError(error.message || 'Não foi possível atualizar a notificação.');
    }
  }

  async function markAllRead() {
    clearError();
    const buttonElement = find('#mark-all');
    if (buttonElement) buttonElement.disabled = true;
    try {
      await request(`${apiBase}/lidas`, { method: 'PATCH' });
      state.notificacoes.forEach(item => { item.lida = true; });
      renderList();
      toast('Todas as notificações foram marcadas como lidas.');
    } catch (error) {
      showError(error.message || 'Não foi possível atualizar as notificações.');
    } finally {
      if (find('#mark-all')) find('#mark-all').disabled = !state.notificacoes.some(item => !item.lida);
    }
  }

  async function changePreference(event) {
    clearError();
    const checkbox = event.currentTarget;
    const wanted = checkbox.checked;
    checkbox.disabled = true;
    try {
      const result = await request(`${apiBase}/preferencia`, {
        method: 'PATCH',
        body: JSON.stringify({ receber: wanted })
      });
      state.receberNotificacoes = result.receberNotificacoes;
      toast(wanted ? 'Notificações ativadas.' : 'Notificações desativadas.');
    } catch (error) {
      checkbox.checked = !wanted;
      showError(error.message || 'Não foi possível salvar sua preferência.');
    } finally {
      checkbox.disabled = false;
    }
  }

  function configureCommonControls() {
    window.lucide?.createIcons();
    const sidebar = $('#sidebar');
    $('#toggle-btn')?.addEventListener('click', () => sidebar?.classList.toggle('expanded'));
    $('#btn-open-logout')?.addEventListener('click', () => {
      if (!window.confirm('Deseja sair da sua conta?')) return;
      const form = document.createElement('form');
      form.method = 'post';
      form.action = '/logout';
      const parameter = $('meta[name="_csrf_parameter"]')?.content || '_csrf';
      if (csrfToken) {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = parameter;
        input.value = csrfToken;
        form.append(input);
      }
      document.body.append(form);
      form.submit();
    });
  }

  async function loadNotifications() {
    root.setAttribute('aria-busy','true');
    try { await reload(); }
    catch(error) {
      root.innerHTML=header('Notificações','Acompanhe as novidades do seu perfil.')+'<p class="mef-error" role="alert"></p>'+button('notification-retry','Tentar novamente');
      find('.mef-error').textContent=error.message||'Não foi possível carregar as notificações.';
      find('#notification-retry').onclick=loadNotifications;
    } finally {root.setAttribute('aria-busy','false');}
  }
  if(!externalNavigation)configureCommonControls();
  window.MEFNotifications={root,reload:loadNotifications};
  root.innerHTML=header('Notificações','Acompanhe as novidades do seu perfil.')+'<p role="status">Carregando notificações…</p>';
  loadNotifications();
})();
