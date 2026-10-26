/*
 * Ponte do chat definitivo do ZIP para o Spring Boot.
 * Preserve CSS/HTML/JS do protótipo; substitui SOMENTE o banco demonstrativo de mensagens.
 * Destinos STOMP, formato da mensagem e endpoint HTTP herdados de chat_prof.html funcional.
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
    const escapeHTML = value => id(value).replace(/[&<>'"]/g, tag =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag]));

    function toast(texto) {
        if (window.MEFUI?.toast) {
            window.MEFUI.toast(texto);
            return;
        }
        console.warn(texto);
        let alert = document.getElementById('chat-status-message');
        if (!alert) {
            alert = document.createElement('div');
            alert.id = 'chat-status-message';
            alert.className = 'mef-toast'; // mesma classe já existente em recursos.css
            alert.setAttribute('role', 'status');
            document.body.append(alert);
        }
        alert.textContent = texto;
    }

    function icons() {
        window.lucide?.createIcons();
    }

    function horaDaMensagem(timestamp) {
        const data = timestamp ? new Date(timestamp) : new Date();
        return Number.isNaN(data.getTime()) ? '' : data.toLocaleTimeString('pt-BR', {
            hour: '2-digit', minute: '2-digit'
        });
    }

    // Mantemos as mesmas classes e hierarquia das bolhas do ZIP original.
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
        for (const mensagem of mensagens) adicionarMensagemNaTela(mensagem);
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
            // Uma resposta atrasada de outro contato não pode substituir a conversa atual.
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
        contactsList.querySelectorAll('.contact-item').forEach(item => item.classList.remove('active'));
        element.classList.add('active');
        usuarioSelecionadoId = id(element.dataset.id);
        activeName.textContent = element.dataset.nome || element.querySelector('.contact-name')?.textContent || '';
        activeAvatar.innerHTML = '<i data-lucide="user"></i>';
        element.querySelector('.unread-dot')?.remove();
        icons();
        wrapper.replaceChildren();
        carregarConversa(usuarioSelecionadoId);
    }

    // O ZIP adiciona pesquisa. Esta versão pesquisa os contatos do servidor,
    // sem substituir nenhum cartão por dados fictícios do localStorage.
    function prepararBusca() {
        const sidebar = document.querySelector('.chat-sidebar');
        const header = sidebar?.querySelector('.chat-sidebar-header');
        if (!sidebar || !header || document.getElementById('contact-search')) return;
        const busca = document.createElement('input');
        busca.id = 'contact-search';
        busca.className = 'chat-text-input contact-search';
        busca.setAttribute('aria-label', 'Pesquisar contatos');
        busca.placeholder = 'Pesquisar contatos...';
        header.insertAdjacentElement('afterend', busca);
        busca.addEventListener('input', () => {
            const q = norm(busca.value);
            contactsList.querySelectorAll('.contact-item').forEach(el => {
                el.hidden = !norm(el.textContent).includes(q);
                // O CSS original força display:flex nos contatos; hidden precisa prevalecer.
                el.style.display = el.hidden ? 'none' : '';
            });
        });
    }

    function receberMensagem(mensagem) {
        const peerId = id(mensagem.sender) === id(usuarioLogadoId)
            ? id(mensagem.recipient) : id(mensagem.sender);
        if (!peerId) return;

        // Se o servidor devolver a mensagem enviada, não duplicar a prévia otimista.
        if (id(mensagem.sender) === id(usuarioLogadoId)) {
            const indice = outgoingPreview.findIndex(p => p.peerId === peerId &&
                p.content === id(mensagem.content) && Date.now() - p.criadoEm < 20000);
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
            content: messageText,
            timestamp: new Date().toISOString()
        };
        stompClient.publish({
            destination: '/app/chat.privateMessage',
            body: JSON.stringify(mensagem)
        });
        const element = adicionarMensagemNaTela(mensagem);
        outgoingPreview.push({ peerId: id(usuarioSelecionadoId), content: messageText, element, criadoEm: Date.now() });
        if (outgoingPreview.length > 100) outgoingPreview.shift();
        input.value = '';
    }

    function handleKeyPress(event) {
        if (event.key === 'Enter' && !event.isComposing) {
            event.preventDefault();
            sendMessage();
        }
    }

    // O protótipo aceitava anexos no localStorage. Enviar ao servidor depende
    // de um endpoint de upload + retorno de metadados, não presente no HTML enviado.
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
            else if (!['image/png','image/jpeg','application/pdf','text/plain'].includes(arquivo.type))
                toast('Selecione um arquivo PNG, JPG, PDF ou TXT.');
            else toast('O envio de anexos requer o endpoint de upload no Spring Boot. O arquivo não foi enviado.');
            fileInput.value = '';
        });
    }

    function iniciarWebSocket() {
        if (usuarioLogadoId === null || usuarioLogadoId === undefined || usuarioLogadoId === '') {
            toast('O backend precisa enviar o atributo usuarioLogadoId para o chat.');
            return;
        }
        if (!window.StompJs?.Client || !window.SockJS) {
            toast('Não foi possível carregar as bibliotecas SockJS/STOMP.');
            return;
        }
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS(config.wsUrl || '/ws'),
            reconnectDelay: 5000,
            onConnect: () => {
                console.log('WebSocket conectado!');
                stompClient.subscribe('/queue/messages/' + usuarioLogadoId, message => {
                    try { receberMensagem(JSON.parse(message.body)); }
                    catch (erro) { console.error('Mensagem STOMP inválida:', erro); }
                });
                // Recarregar após reconexão para recuperar eventos perdidos.
                if (usuarioSelecionadoId) carregarConversa(usuarioSelecionadoId);
            },
            onStompError: frame => console.error('Erro STOMP:', frame),
            onWebSocketError: error => console.error('Erro WebSocket:', error)
        });
        stompClient.activate();
    }

    // O HTML definitivo chama essas funções nas mesmas ações de usuário.
    window.selectContact = selectContact;
    window.sendMessage = sendMessage;
    window.handleKeyPress = handleKeyPress;

    prepararBusca();
    prepararAnexos();
    iniciarWebSocket();
    const primeiroContato = contactsList.querySelector('.contact-item');
    if (primeiroContato) selectContact(primeiroContato);
    else {
        activeName.textContent = 'Nenhum contato disponível';
        input.disabled = true;
        sendButton.disabled = true;
    }
})();
