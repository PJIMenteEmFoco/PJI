package br.edu.ifsp.mef.controller;

import br.edu.ifsp.mef.service.MensagemChatService;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import br.edu.ifsp.mef.model.MensagemChat;
import br.edu.ifsp.mef.model.MensagemChatEntity;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.MensagemChatRepository;

@Controller
public class ChatController {

	@Autowired
	MensagemChatService mensagemChatService;
	@Autowired
	SimpMessagingTemplate templateMensagem;
	@Autowired
	MensagemChatRepository mensagemChatRepository;

	@MessageMapping("/chat.privateMessage")
	public void sendPrivateMessage(MensagemChat mensagemChat) {

	    MensagemChatEntity mensagem = new MensagemChatEntity();

	    mensagem.setSender(mensagemChat.getSender());
	    mensagem.setRecipient(mensagemChat.getRecipient());
	    mensagem.setContent(mensagemChat.getContent());
	    mensagem.setTimestamp(LocalDateTime.now());

	    // 1. Salva no banco
	    mensagemChatRepository.save(mensagem);

	    // 2. Envia em tempo real direto para a fila específica do destinatário baseada no ID
	    templateMensagem.convertAndSend(
	        "/queue/messages/" + mensagemChat.getRecipient(), 
	        mensagem
	    );
	}

	@GetMapping("/chat/{idUsuario}")
	@ResponseBody
	public List<MensagemChatEntity> buscarConversa(@PathVariable Long idUsuario, Authentication authentication) {

		UsuarioDetails usuario = (UsuarioDetails) authentication.getPrincipal();

		return mensagemChatService.buscarConversa(String.valueOf(usuario.getId()), String.valueOf(idUsuario));
	}

	@GetMapping("/professor/chat/historico/{idUsuario}")
	@ResponseBody
	public List<MensagemChatEntity> buscarConversaProfessor(@PathVariable Long idUsuario,
			Authentication authentication) {
		UsuarioDetails usuario = (UsuarioDetails) authentication.getPrincipal();
		return mensagemChatService.buscarConversa(String.valueOf(usuario.getId()), String.valueOf(idUsuario));
	}

	@GetMapping("/aluno/chat/historico/{idUsuario}")
	@ResponseBody
	public List<MensagemChatEntity> buscarConversaAluno(@PathVariable Long idUsuario, Authentication authentication) {
		UsuarioDetails usuario = (UsuarioDetails) authentication.getPrincipal();
		return mensagemChatService.buscarConversa(String.valueOf(usuario.getId()), String.valueOf(idUsuario));
	}

}
