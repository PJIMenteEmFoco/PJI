/* Adaptador para a rota /professor/configuracoes.
 * Mantem intocados os JavaScripts do ZIP e impede que os dados demonstrativos
 * ou redirecionamentos estaticos sobrescrevam as informacoes do servidor.
 */
(() => {
  'use strict';
  if (document.body?.dataset.mefBackend !== 'true') return;

  const $ = (selector) => document.querySelector(selector);
  const body = document.body;
  const userKey = body.dataset.mefUserId || 'professor';
  const nameEl = $('#val-name');
  const phoneEl = $('#val-phone');
  const avatar = $('.profile-avatar-large');
  const editModal = $('#edit-modal');
  const input = $('#modal-field-input');
  const title = $('#modal-title-text');
  const csrfToken = $('meta[name="_csrf"]')?.content || '';
  const csrfHeader = $('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
  const prefsKey = `mef-configuracoes:${userKey}:notificacoes`;
  const photoKey = `mef-configuracoes:${userKey}:foto-local`;
  let fieldInEdit = null;
  let isSaving = false;

  const toast = (message) => {
    if (window.MEFUI?.toast) { window.MEFUI.toast(message); return; }
    const old = document.querySelector('[data-mef-settings-feedback]');
    old?.remove();
    const el = document.createElement('div');
    el.dataset.mefSettingsFeedback = '';
    el.setAttribute('role', 'status');
    el.style.cssText = 'position:fixed;bottom:22px;right:22px;background:#0f2d3c;color:#fff;padding:12px 18px;border-radius:12px;z-index:9999;max-width:360px;box-shadow:0 8px 22px #0003';
    el.textContent = message;
    document.body.append(el);
    setTimeout(() => el.remove(), 5500);
  };

  // O arquivo original perfil-ajustes.js preenche dados ficticios ao carregar.
  // Aplicamos novamente as informacoes reais apos a execucao dos scripts.
  // Quando o template eh renderizado, os data-* vem do Model do Controller.
  if (nameEl) nameEl.textContent = body.dataset.mefProfileName?.trim() || 'Não informado';
  if (phoneEl) phoneEl.textContent = body.dataset.mefProfilePhone?.trim() || 'Não informado';

  const showPhoto = (source) => {
    if (!avatar || !source) return;
    const img = document.createElement('img');
    img.src = source;
    img.alt = 'Foto de perfil';
    img.className = 'profile-photo';
    avatar.replaceChildren(img);
  };
  if (avatar) {
    // Nao exibir uma foto demonstrativa de outro usuario.
    avatar.replaceChildren();
    const icon = document.createElement('i');
    icon.setAttribute('data-lucide', 'user');
    avatar.append(icon);
    try { showPhoto(localStorage.getItem(photoKey)); } catch (_) { /* sem armazenamento */ }
    window.lucide?.createIcons();
  }

  // Bloqueia exclusivamente as rotinas antigas de demonstracao nos toggles.
  // Preferencias continuam locais; ainda nao existem endpoints fornecidos.
  const toggles = [...document.querySelectorAll('.btn-toggle-status')];
  let prefs = {};
  try { prefs = JSON.parse(localStorage.getItem(prefsKey) || '{}') || {}; } catch (_) { prefs = {}; }
  const applyToggle = (button, enabled) => {
    button.classList.toggle('disabled', !enabled);
    button.textContent = enabled ? 'Ativado' : 'Desativado';
    button.setAttribute('aria-pressed', String(enabled));
  };
  toggles.forEach((button, index) => {
    const enabled = typeof prefs[index] === 'boolean' ? prefs[index] : !button.classList.contains('disabled');
    applyToggle(button, enabled);
    button.addEventListener('click', (event) => {
      event.preventDefault();
      event.stopImmediatePropagation();
      const next = button.classList.contains('disabled');
      try {
        prefs[index] = next;
        localStorage.setItem(prefsKey, JSON.stringify(prefs));
        applyToggle(button, next);
      } catch (_) { toast('Não foi possível salvar a preferência neste navegador.'); }
    }, true);
  });

  window.openEditModal = (label, targetId) => {
    if (label === 'Foto de Perfil') {
      const picker = document.createElement('input');
      picker.type = 'file';
      picker.accept = '.png,.jpg,.jpeg,image/png,image/jpeg';
      picker.onchange = () => {
        const file = picker.files?.[0];
        if (!file) return;
        if (file.size > 524288 || !['image/png', 'image/jpeg'].includes(file.type)) {
          toast('Selecione uma imagem PNG ou JPEG de até 512 KB.');
          return;
        }
        const reader = new FileReader();
        reader.onload = () => {
          try {
            localStorage.setItem(photoKey, String(reader.result));
            showPhoto(String(reader.result));
            toast('Foto salva neste navegador. O envio para o servidor ainda depende de uma API de upload.');
          } catch (_) { toast('Não foi possível salvar a foto neste navegador.'); }
        };
        reader.onerror = () => toast('Não foi possível ler a imagem.');
        reader.readAsDataURL(file);
      };
      picker.click();
      return;
    }
    if (label === 'Senha') {
      toast('A troca de senha depende da rota de recuperação de senha do Spring Boot.');
      return;
    }
    if (!['val-name', 'val-phone'].includes(targetId)) return;
    fieldInEdit = targetId === 'val-name' ? 'nome' : 'telefone';
    title.textContent = `Editar ${label}`;
    input.type = fieldInEdit === 'telefone' ? 'tel' : 'text';
    const current = document.getElementById(targetId)?.textContent?.trim() || '';
    input.value = current === 'Não informado' ? '' : current;
    editModal?.classList.add('active');
    input?.focus();
  };

  window.saveEditModal = async () => {
    if (!fieldInEdit || isSaving) return;
    const value = input?.value.trim() || '';
    if (!value) { toast('Preencha o campo antes de salvar.'); input?.focus(); return; }
    if (fieldInEdit === 'telefone' && !/^[+()\d\s-]{8,20}$/.test(value)) {
      toast('Informe um telefone válido.'); return;
    }
    const saveButton = $('#edit-modal .btn-modal-save');
    isSaving = true;
    if (saveButton) saveButton.disabled = true;
    try {
      // CONTRATO: implementar POST /professor/configuracoes/dados no backend.
      const headers = { 'Content-Type': 'application/json' };
      if (csrfToken) headers[csrfHeader] = csrfToken;
      const response = await fetch('/professor/configuracoes/dados', {
        method: 'POST',
        headers,
        credentials: 'same-origin',
        body: JSON.stringify({ campo: fieldInEdit, valor: value })
      });
      if (!response.ok) throw new Error(`Servidor retornou HTTP ${response.status}`);
      const el = fieldInEdit === 'nome' ? nameEl : phoneEl;
      if (el) el.textContent = value;
      if (fieldInEdit === 'nome') body.dataset.mefProfileName = value;
      else body.dataset.mefProfilePhone = value;
      editModal?.classList.remove('active');
      toast('Alteração salva com sucesso.');
    } catch (error) {
      console.error('Falha ao atualizar configurações:', error);
      toast('Não foi possível salvar. Confira a rota de atualização no Spring Boot.');
    } finally {
      isSaving = false;
      if (saveButton) saveButton.disabled = false;
    }
  };

  // A exclusao de conta nao pode ser simulada como se tivesse sido confirmada.
  window.confirmDeleteAccount = () => {
    $('#delete-modal')?.classList.remove('active');
    toast('Exclusão não executada: é necessário implementar um endpoint autenticado.');
  };

  // O JS original redireciona ao login .html. Na aplicação real o logout
  // precisa ser feito por POST para o Spring Security (com CSRF).
  $('#btn-confirm-logout')?.addEventListener('click', (event) => {
    event.preventDefault();
    event.stopImmediatePropagation();
    const form = $('#mef-logout-form');
    if (form) form.submit();
    else toast('Formulário de logout não encontrado.');
  }, true);
})();
