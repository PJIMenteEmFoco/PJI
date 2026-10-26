/* Controles comuns específicos da interface integrada do Mente em Foco. */
(() => {
    'use strict';

    const byId = (id) => document.getElementById(id);
    const sidebar = byId('sidebar');
    const toggleButton = byId('toggle-btn');
    const logoutModal = byId('logout-modal');

    // Aplica a preferência de tema compartilhada com as demais telas.
    if (window.MEFTheme?.apply) {
        window.MEFTheme.apply(window.MEFTheme.current());
    } else {
        try {
            if (localStorage.getItem('theme') === 'escuro') {
                document.body.classList.add('dark-mode');
            }
        } catch (_) {
            // A tela continua funcional mesmo quando o navegador bloqueia o storage.
        }
    }

    window.lucide?.createIcons();

    const toggleSidebar = () => sidebar?.classList.toggle('expanded');
    toggleButton?.addEventListener('click', toggleSidebar);
    toggleButton?.addEventListener('keydown', (event) => {
        // revisao.js já encaminha Enter/Espaço para click nessas páginas.
        if (event.defaultPrevented) return;
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            toggleSidebar();
        }
    });

    const openLogout = () => {
        if (!logoutModal) return;
        logoutModal.classList.add('active');
        logoutModal.setAttribute('aria-hidden', 'false');
        byId('btn-cancel-logout')?.focus();
    };

    const closeLogout = () => {
        if (!logoutModal) return;
        logoutModal.classList.remove('active');
        logoutModal.setAttribute('aria-hidden', 'true');
        byId('btn-open-logout')?.focus();
    };

    byId('btn-open-logout')?.addEventListener('click', openLogout);
    byId('btn-cancel-logout')?.addEventListener('click', closeLogout);

    // Clicar fora do cartão ou pressionar Esc fecha a confirmação.
    logoutModal?.addEventListener('click', (event) => {
        if (event.target === logoutModal) closeLogout();
    });
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && logoutModal?.classList.contains('active')) {
            closeLogout();
        }
    });
})();
