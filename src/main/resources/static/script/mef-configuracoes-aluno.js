/* Integração backend da tela de configurações do aluno.
 * Preserva o layout e os scripts originais do PJI-main; impede ações demonstrativas
 * de fingirem que foto, preferências, senha ou exclusão foram persistidas no servidor.
 */
(() => {
  'use strict';
  const body = document.body;
  if (body?.dataset.mefBackend !== 'true') return;

  const $ = (selector) => document.querySelector(selector);
  const userKey = body.dataset.mefUserId || 'aluno';
  const nameEl = $('#val-name');
  const phoneEl = $('#val-phone');
  const avatar = $('.profile-avatar-large');
  const editModal = $('#edit-modal');
  const input = $('#modal-field-input');
  const title = $('#modal-title-text');
  const csrfToken = $('meta[name="_csrf"]')?.content || '';
  const csrfHeader = $('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
  const saveUrl = body.dataset.mefConfigEndpoint || '/aluno/configuracoes/dados';
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
    window.setTimeout(() => el.remove(), 5500);
  };

  // Os scripts de protótipo podem preencher nomes demonstrativos; o dado do servidor prevalece.
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
    avatar.replaceChildren();
    const icon = document.createElement('i');
    icon.setAttribute('data-lucide', 'user');
    avatar.append(icon);
    try { showPhoto(localStorage.getItem(photoKey)); } catch (_) { /* armazenamento indisponível */ }
    window.lucide?.createIcons();
  }

  // Preferências de notificação permanecem locais até existir endpoint próprio no backend.
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
        toast('Preferência salva neste navegador. Ela ainda não é sincronizada com o servidor.');
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
      toast('Para trocar a senha, utilize o fluxo de recuperação de senha já integrado.');
      return;
    }
    if (!['val-name', 'val-phone'].includes(targetId)) return;
    fieldInEdit = targetId === 'val-name' ? 'nome' : 'telefone';
    if (title) title.textContent = `Editar ${label}`;
    if (input) {
      input.type = fieldInEdit === 'telefone' ? 'tel' : 'text';
      const current = document.getElementById(targetId)?.textContent?.trim() || '';
      input.value = current === 'Não informado' ? '' : current;
    }
    editModal?.classList.add('active');
    input?.focus();
  };

  window.saveEditModal = async () => {
    if (!fieldInEdit || isSaving) return;
    const value = input?.value.trim() || '';
    if (!value) { toast('Preencha o campo antes de salvar.'); input?.focus(); return; }
    if (fieldInEdit === 'nome' && (value.length < 2 || value.length > 150)) {
      toast('O nome deve ter de 2 a 150 caracteres.'); return;
    }
    if (fieldInEdit === 'telefone') {
      if (!/^\+?[0-9()\s-]{8,20}$/.test(value)) { toast('Informe um telefone válido.'); return; }
      const digits = value.replace(/\D/g, '').length;
      if (digits < 8 || digits > 15) { toast('O telefone deve conter de 8 a 15 dígitos.'); return; }
    }
    const saveButton = $('#edit-modal .btn-modal-save');
    isSaving = true;
    if (saveButton) saveButton.disabled = true;
    try {
      const headers = { 'Content-Type': 'application/json' };
      if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
      const response = await fetch(saveUrl, {
        method: 'POST',
        headers,
        credentials: 'same-origin',
        body: JSON.stringify({ campo: fieldInEdit, valor: value })
      });
      let data = {};
      try { data = await response.json(); } catch (_) { /* erro HTTP sem JSON */ }
      if (!response.ok || data.sucesso === false) {
        throw new Error(data.mensagem || `Servidor retornou HTTP ${response.status}`);
      }
      const savedValue = data.valor || value;
      const element = fieldInEdit === 'nome' ? nameEl : phoneEl;
      if (element) element.textContent = savedValue;
      if (fieldInEdit === 'nome') body.dataset.mefProfileName = savedValue;
      else body.dataset.mefProfilePhone = savedValue;
      editModal?.classList.remove('active');
      toast(data.mensagem || 'Alteração salva com sucesso.');
    } catch (error) {
      console.error('Falha ao atualizar configurações do aluno:', error);
      toast(error.message || 'Não foi possível salvar. Confira a conexão e tente novamente.');
    } finally {
      isSaving = false;
      if (saveButton) saveButton.disabled = false;
    }
  };

  // Não simula exclusão se ainda não existe endpoint seguro para essa operação.
  window.confirmDeleteAccount = () => {
    $('#delete-modal')?.classList.remove('active');
    toast('Exclusão não executada: é necessário implementar um endpoint autenticado.');
  };

  // Impede o redirecionamento antigo para login.html e usa o logout do Spring Security.
  $('#btn-confirm-logout')?.addEventListener('click', (event) => {
    event.preventDefault();
    event.stopImmediatePropagation();
    const form = $('#mef-logout-form');
    if (form) form.submit();
    else toast('Formulário de logout não encontrado.');
  }, true);
})();
