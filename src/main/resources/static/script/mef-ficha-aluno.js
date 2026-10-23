(() => {
    'use strict';

    const $ = (selector, root = document) => root.querySelector(selector);

    function showToast(message) {
        let toast = $('#mef-ficha-toast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'mef-ficha-toast';
            toast.className = 'mef-toast';
            toast.setAttribute('role', 'status');
            document.body.appendChild(toast);
        }
        toast.textContent = message;
        toast.hidden = false;
        window.setTimeout(() => {
            if (toast.isConnected) toast.remove();
        }, 3200);
    }

    function showError(message) {
        const element = $('#ficha-edit-error');
        if (!element) return;
        element.textContent = message;
        element.hidden = false;
    }

    function clearError() {
        const element = $('#ficha-edit-error');
        if (!element) return;
        element.textContent = '';
        element.hidden = true;
    }

    function textOrFallback(value) {
        const text = String(value ?? '').trim();
        return text || 'Não informado';
    }

    function formatTempoFoco(value) {
        const number = Number(value);
        if (!Number.isFinite(number) || number <= 0) return 'Não informado';
        return number.toLocaleString('pt-BR', { maximumFractionDigits: 2 });
    }

    document.addEventListener('DOMContentLoaded', () => {
        window.lucide?.createIcons();

        const sidebar = $('#sidebar');
        $('#toggle-btn')?.addEventListener('click', () => sidebar?.classList.toggle('expanded'));

        const editDialog = $('#editar-ficha-dialog');
        $('#btn-editar-ficha')?.addEventListener('click', () => {
            clearError();
            editDialog?.showModal();
        });
        $('#cancelar-edicao')?.addEventListener('click', () => editDialog?.close());

        const logoutDialog = $('#logout-dialog');
        $('#btn-open-logout')?.addEventListener('click', () => logoutDialog?.showModal());
        $('#cancelar-logout')?.addEventListener('click', () => logoutDialog?.close());

        const form = $('#editar-ficha-form');
        const root = $('#mef-root');
        if (!form || !root) return;

        form.addEventListener('submit', async event => {
            event.preventDefault();
            clearError();

            const saveButton = $('#salvar-ficha');
            const id = root.dataset.alunoId;
            const rawTempo = form.elements.tempoFoco.value.trim();
            const tempoFoco = rawTempo === '' ? 0 : Number(rawTempo);

            if (!id) {
                showError('Não foi possível identificar o aluno desta ficha.');
                return;
            }
            if (!form.elements.nome.value.trim()) {
                showError('O nome do aluno é obrigatório.');
                form.elements.nome.focus();
                return;
            }
            if (!Number.isFinite(tempoFoco) || tempoFoco < 0 || tempoFoco > 1440) {
                showError('Informe um tempo de foco entre 0 e 1440.');
                form.elements.tempoFoco.focus();
                return;
            }

            const payload = {
                nome: form.elements.nome.value.trim(),
                telefone: form.elements.telefone.value.trim(),
                hiperfoco: form.elements.hiperfoco.value.trim(),
                tempoFoco,
                desregulacao: form.elements.desregulacao.value.trim(),
                laudo: form.elements.laudo.value.trim()
            };

            const csrfToken = $('meta[name="_csrf"]')?.content;
            const csrfHeader = $('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken) headers[csrfHeader] = csrfToken;

            saveButton.disabled = true;
            saveButton.textContent = 'Salvando...';

            try {
                const response = await fetch(
                    `/professor/alunos/${encodeURIComponent(id)}/ficha/dados`,
                    {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers,
                        body: JSON.stringify(payload)
                    }
                );

                let result = {};
                try {
                    result = await response.json();
                } catch {
                    // Abaixo mostramos uma mensagem genérica se o servidor não devolver JSON.
                }

                if (!response.ok) {
                    throw new Error(result.message || result.mensagem || 'Não foi possível salvar os dados do aluno.');
                }

                const aluno = result.aluno || payload;
                $('#ficha-nome').textContent = textOrFallback(aluno.nome);
                $('#ficha-telefone').textContent = textOrFallback(aluno.telefone);
                $('#ficha-hiperfoco').textContent = textOrFallback(aluno.hiperfoco);
                $('#ficha-tempo-foco').textContent = formatTempoFoco(aluno.tempoFoco);
                $('#ficha-desregulacao').textContent = textOrFallback(aluno.desregulacao);
                $('#ficha-laudo').textContent = textOrFallback(aluno.laudo);

                editDialog?.close();
                showToast(result.mensagem || 'Ficha do aluno atualizada com sucesso.');
            } catch (error) {
                showError(error?.message || 'Ocorreu um erro ao salvar os dados.');
            } finally {
                saveButton.disabled = false;
                saveButton.textContent = 'Salvar alterações';
            }
        });
    });
})();
