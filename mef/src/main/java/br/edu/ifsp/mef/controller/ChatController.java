package br.edu.ifsp.mef.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import br.edu.ifsp.mef.model.MensagemChat;
import br.edu.ifsp.mef.model.MensagemChatEntity;
import br.edu.ifsp.mef.repository.MensagemChatRepository;

@Controller
public class ChatController {

	@Autowired
	SimpMessagingTemplate templateMensagem;
	@Autowired
	MensagemChatRepository mensagemChatRepository;

	public ChatController(SimpMessagingTemplate templateMensagem, MensagemChatRepository mensagemChatRepository) {
		this.templateMensagem = templateMensagem;
		this.mensagemChatRepository = mensagemChatRepository;
	}

	@MessageMapping("/chat.privateMessage")
	public void sendPrivateMessage(MensagemChat mensagemChat) {

		MensagemChatEntity mensagem = new MensagemChatEntity();
		mensagem.setSender(mensagemChat.getSender());
		mensagem.setRecipient(mensagemChat.getRecipient());
		mensagem.setContent(mensagemChat.getContent());
		mensagem.setTimestamp(LocalDateTime.now());

		mensagemChatRepository.save(mensagem);

		templateMensagem.convertAndSendToUser(mensagemChat.getRecipient(), "/queue/messages", mensagemChat);
	}
}
