/* Integração do chat do aluno com o backend Spring Boot.
 * Mantém as classes do chatAluno.css e usa o histórico HTTP e o WebSocket já existentes.
 */
(() => {
    'use strict';

    if (document.body?.dataset.chatMode !== 'backend') return;

    const config = window.MEF_CHAT_CONFIG || {};
    const usuarioLogadoId = config.usuarioLogadoId;
    const wrapper = document.getElementById('messages-wrapper');
    const contactsList = document.getElementById('contacts-list');
    const input = document.getElementById('chat-input');
    const sendButton = document.querySelector('.btn-send-msg');
    const attachButton = document.querySelector('.btn-plus-attach');
    const activeName = document.getElementById('active-contact-name');
    const activeAvatar = document.getElementById('active-avatar-box');

    if (!wrapper || !contactsList || !input || !sendButton) return;

    let usuarioSelecionadoId = null;
    let historyRequestId = 0;
    let stompClient = null;
    const outgoingPreview = [];

    const id = value => String(value ?? '');
    const norm = value => id(value).normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR');
    const escapeHTML = value => id(value).replace(/[&<>"']/g, tag =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[tag]));

    function toast(texto) {
        if (window.MEFUI?.toast) {
            window.MEFUI.toast(texto);
            return;
        }
        let alert = document.getElementById('chat-status-message');
        if (!alert) {
            alert = document.createElement('div');
            alert.id = 'chat-status-message';
            alert.className = 'chat-status-message';
            alert.setAttribute('role', 'status');
            alert.setAttribute('aria-live', 'polite');
            document.body.append(alert);
        }
        alert.textContent = texto;
    }

    function icons() {
        window.lucide?.createIcons();
    }

    function horaDaMensagem(timestamp) {
        let data;
        if (Array.isArray(timestamp) && timestamp.length >= 5) {
            data = new Date(timestamp[0], timestamp[1] - 1, timestamp[2], timestamp[3] || 0, timestamp[4] || 0, timestamp[5] || 0);
        } else {
            data = timestamp ? new Date(timestamp) : new Date();
        }
        if (Number.isNaN(data.getTime())) return '';
        return data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    }

    function adicionarMensagemNaTela(mensagem) {
        const souEu = id(mensagem.sender) === id(usuarioLogadoId);
        const linha = document.createElement('div');
        linha.className = `message-row ${souEu ? 'sent' : 'received'}`;
        const hora = horaDaMensagem(mensagem.timestamp);
        const conteudo = escapeHTML(mensagem.content);

        linha.innerHTML = souEu
            ? `<div class="message-bubble">
                   <span>${conteudo}</span>
                   <div class="message-meta">
                       <span>${hora}</span>
                       <i data-lucide="check-check" class="read-check"></i>
                   </div>
               </div>`
            : `<div class="avatar-box" style="width:36px; height:36px; border:none;">
                   <i data-lucide="user"></i>
               </div>
               <div class="message-bubble">
                   <span>${conteudo}</span>
                   <div class="message-meta" style="color:#557D91;">${hora}</div>
               </div>`;

        wrapper.appendChild(linha);
        icons();
        wrapper.scrollTop = wrapper.scrollHeight;
        return linha;
    }

    function renderMessages(mensagens) {
        wrapper.replaceChildren();
        if (!mensagens.length) {
            const empty = document.createElement('p');
            empty.className = 'chat-empty';
            empty.textContent = 'Nenhuma mensagem nesta conversa. Envie uma mensagem para começar.';
            wrapper.appendChild(empty);
        } else {
            for (const mensagem of mensagens) adicionarMensagemNaTela(mensagem);
        }
        wrapper.scrollTop = wrapper.scrollHeight;
    }

    async function carregarConversa(idUsuario) {
        const requestId = ++historyRequestId;
        try {
            const response = await fetch(config.historicoBase + encodeURIComponent(idUsuario), {
                credentials: 'same-origin',
                headers: { Accept: 'application/json' }
            });
            if (!response.ok) throw new Error(`Falha ao carregar histórico (HTTP ${response.status}).`);
            const mensagens = await response.json();
            if (requestId === historyRequestId && id(usuarioSelecionadoId) === id(idUsuario)) {
                if (!Array.isArray(mensagens)) throw new Error('A resposta do histórico não é uma lista.');
                renderMessages(mensagens);
            }
        } catch (erro) {
            if (requestId === historyRequestId) {
                console.error('Erro ao carregar conversa:', erro);
                toast('Não foi possível carregar o histórico da conversa.');
            }
        }
    }

    function selectContact(element) {
        if (!element) return;
        const selectedId = id(element.dataset.id);
        if (!selectedId) return;
        contactsList.querySelectorAll('.contact-item').forEach(item => item.classList.remove('active'));
        element.classList.add('active');
        usuarioSelecionadoId = selectedId;
        if (activeName) activeName.textContent = element.dataset.nome || element.querySelector('.contact-name')?.textContent || '';
        if (activeAvatar) activeAvatar.innerHTML = '<i data-lucide="user"></i>';
        element.querySelector('.unread-dot')?.remove();
        icons();
        wrapper.replaceChildren();
        carregarConversa(usuarioSelecionadoId);
    }

    function prepararBusca() {
        const sidebar = document.querySelector('.chat-sidebar');
        const header = sidebar?.querySelector('.chat-sidebar-header');
        if (!sidebar || !header || document.getElementById('contact-search')) return;
        const busca = document.createElement('input');
        busca.id = 'contact-search';
        busca.className = 'chat-text-input contact-search';
        busca.type = 'search';
        busca.setAttribute('aria-label', 'Pesquisar contatos');
        busca.placeholder = 'Pesquisar contatos...';
        header.insertAdjacentElement('afterend', busca);
        busca.addEventListener('input', () => {
            const query = norm(busca.value);
            contactsList.querySelectorAll('.contact-item').forEach(element => {
                const ocultar = !norm(element.textContent).includes(query);
                element.hidden = ocultar;
                element.style.display = ocultar ? 'none' : '';
            });
        });
    }

    function receberMensagem(mensagem) {
        const peerId = id(mensagem.sender) === id(usuarioLogadoId)
            ? id(mensagem.recipient) : id(mensagem.sender);
        if (!peerId) return;

        if (id(mensagem.sender) === id(usuarioLogadoId)) {
            const indice = outgoingPreview.findIndex(item => item.peerId === peerId &&
                item.content === id(mensagem.content) && Date.now() - item.criadoEm < 20000);
            if (indice !== -1) {
                const previa = outgoingPreview.splice(indice, 1)[0];
                const hora = previa.element.querySelector('.message-meta span');
                if (hora) hora.textContent = horaDaMensagem(mensagem.timestamp);
                return;
            }
        }

        if (peerId === id(usuarioSelecionadoId)) {
            adicionarMensagemNaTela(mensagem);
            return;
        }
        const contato = [...contactsList.querySelectorAll('.contact-item')]
            .find(item => id(item.dataset.id) === peerId);
        if (contato) {
            const avatar = contato.querySelector('.avatar-box');
            if (avatar && !avatar.querySelector('.unread-dot')) {
                const dot = document.createElement('span');
                dot.className = 'unread-dot';
                avatar.appendChild(dot);
            }
        }
    }

    function sendMessage() {
        const messageText = input.value.trim();
        if (!messageText) return;
        if (!usuarioSelecionadoId) return toast('Selecione um contato antes de enviar.');
        if (!stompClient?.connected) return toast('O chat está desconectado. Tente novamente quando reconectar.');

        const mensagem = {
            sender: id(usuarioLogadoId),
            recipient: id(usuarioSelecionadoId),
            content: messageText
        };
        stompClient.publish({
            destination: '/app/chat.privateMessage',
            body: JSON.stringify(mensagem)
        });
        const preview = adicionarMensagemNaTela({ ...mensagem, timestamp: new Date().toISOString() });
        outgoingPreview.push({ peerId: id(usuarioSelecionadoId), content: messageText, element: preview, criadoEm: Date.now() });
        if (outgoingPreview.length > 100) outgoingPreview.shift();
        input.value = '';
        input.focus();
    }

    function handleKeyPress(event) {
        if (event.key === 'Enter' && !event.isComposing) {
            event.preventDefault();
            sendMessage();
        }
    }

    function prepararAnexos() {
        if (!attachButton) return;
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.accept = 'image/png,image/jpeg,application/pdf,text/plain';
        fileInput.hidden = true;
        document.body.appendChild(fileInput);
        attachButton.addEventListener('click', () => {
            if (!usuarioSelecionadoId) return toast('Selecione um contato antes de anexar.');
            fileInput.click();
        });
        fileInput.addEventListener('change', () => {
            const arquivo = fileInput.files?.[0];
            if (!arquivo) return;
            if (arquivo.size > 1048576) toast('O arquivo deve ter no máximo 1 MB.');
            else if (!['image/png', 'image/jpeg', 'application/pdf', 'text/plain'].includes(arquivo.type))
                toast('Selecione um arquivo PNG, JPG, PDF ou TXT.');
            else toast('O backend atual não possui endpoint de upload para anexos; o arquivo não foi enviado.');
            fileInput.value = '';
        });
    }

    function configurarEstruturaDaTela() {
        icons();
        const sidebar = document.getElementById('sidebar');
        const toggle = document.getElementById('toggle-btn');
        toggle?.addEventListener('click', () => sidebar?.classList.toggle('expanded'));
        toggle?.addEventListener('keydown', event => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                sidebar?.classList.toggle('expanded');
            }
        });

        const modal = document.getElementById('logout-modal');
        const open = document.getElementById('btn-open-logout');
        const cancel = document.getElementById('btn-cancel-logout');
        const confirm = document.getElementById('btn-confirm-logout');
        const logoutForm = document.getElementById('logoutForm');
        const closeModal = () => {
            modal?.classList.remove('active');
            modal?.setAttribute('aria-hidden', 'true');
        };
        open?.addEventListener('click', event => {
            event.preventDefault();
            modal?.classList.add('active');
            modal?.setAttribute('aria-hidden', 'false');
        });
        cancel?.addEventListener('click', closeModal);
        modal?.addEventListener('click', event => { if (event.target === modal) closeModal(); });
        document.addEventListener('keydown', event => { if (event.key === 'Escape') closeModal(); });
        confirm?.addEventListener('click', () => {
            if (logoutForm) logoutForm.requestSubmit();
            else toast('Não foi possível iniciar a saída da conta.');
        });
    }

    window.selectContact = selectContact;
    window.sendMessage = sendMessage;
    window.handleKeyPress = handleKeyPress;

    configurarEstruturaDaTela();
    prepararBusca();
    prepararAnexos();

    if (usuarioLogadoId === null || usuarioLogadoId === undefined || usuarioLogadoId === '') {
        toast('O backend precisa enviar o identificador do aluno para o chat.');
        input.disabled = true;
        sendButton.disabled = true;
        return;
    }

    if (!window.StompJs?.Client || !window.SockJS) {
        toast('Não foi possível carregar as bibliotecas SockJS/STOMP.');
        input.disabled = true;
        sendButton.disabled = true;
        return;
    }

    stompClient = new StompJs.Client({
        webSocketFactory: () => new SockJS(config.wsUrl || '/ws'),
        reconnectDelay: 5000,
        onConnect: () => {
            stompClient.subscribe('/queue/messages/' + usuarioLogadoId, message => {
                try { receberMensagem(JSON.parse(message.body)); }
                catch (erro) { console.error('Mensagem STOMP inválida:', erro); }
            });
            if (usuarioSelecionadoId) carregarConversa(usuarioSelecionadoId);
        },
        onStompError: frame => { console.error('Erro STOMP:', frame); toast('A conexão do chat retornou um erro.'); },
        onWebSocketError: error => console.error('Erro WebSocket:', error)
    });
    stompClient.activate();

    const primeiroContato = contactsList.querySelector('.contact-item');
    if (primeiroContato) selectContact(primeiroContato);
    else {
        if (activeName) activeName.textContent = 'Nenhum contato disponível';
        input.disabled = true;
        sendButton.disabled = true;
    }

    window.addEventListener('beforeunload', () => { if (stompClient?.active) stompClient.deactivate(); });
})();
