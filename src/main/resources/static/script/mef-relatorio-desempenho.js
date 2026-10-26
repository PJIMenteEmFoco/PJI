/* Liga o relatório aos dados persistidos da tentativa atual. */
(() => {
    'use strict';
    const byId = (id) => document.getElementById(id);
    const retry = byId('btn-retry');
    retry?.addEventListener('click', () => {
        const id = retry.dataset.atividadeId;
        if (!id || !/^\d+$/.test(id) || Number(id) <= 0) {
            window.location.assign('/aluno/tarefas');
            return;
        }
        window.location.assign(`/aluno/tarefas/${encodeURIComponent(id)}/executar`);
    });
    byId('btn-confirm-logout')?.addEventListener('click', () => byId('mef-logout-form')?.requestSubmit());
    // Evita manter um botão de repetição ativo sem ID de atividade fornecido pelo servidor.
    if (retry && !retry.dataset.atividadeId) retry.disabled = true;
})();
