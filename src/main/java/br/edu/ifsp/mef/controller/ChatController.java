package br.edu.ifsp.mef.controller;

import br.edu.ifsp.mef.service.MensagemChatService;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.mef.model.MensagemChat;
import br.edu.ifsp.mef.model.MensagemChatEntity;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.MensagemChatRepository;
import br.edu.ifsp.mef.repository.TurmaRepository;

@Controller
public class ChatController {

	@Autowired
	MensagemChatService mensagemChatService;
	@Autowired
	SimpMessagingTemplate templateMensagem;
	@Autowired
	MensagemChatRepository mensagemChatRepository;
	@Autowired
	TurmaRepository turmaRepository;

	@MessageMapping("/chat.privateMessage")
	public void sendPrivateMessage(MensagemChat mensagemChat) {

		MensagemChatEntity mensagem = new MensagemChatEntity();

		mensagem.setSender(mensagemChat.getSender());
		mensagem.setRecipient(mensagemChat.getRecipient());
		mensagem.setContent(mensagemChat.getContent());
		mensagem.setTimestamp(LocalDateTime.now());

		mensagemChatRepository.save(mensagem);

		templateMensagem.convertAndSend("/queue/messages/" + mensagemChat.getRecipient(), mensagem);
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

	@GetMapping("/psicopedagogo/chat/historico/{idUsuario}")
	@ResponseBody
	public List<MensagemChatEntity> buscarConversaPsicopedagogo(@PathVariable Long idUsuario,
			Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails psicopedagogo)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
		}

		if (psicopedagogo.getUsuario().getPerfil() == null
				|| !"PSICOPEDAGOGO".equalsIgnoreCase(psicopedagogo.getUsuario().getPerfil())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso permitido apenas ao psicopedagogo.");
		}

		boolean alunoVinculado = turmaRepository.buscarAlunosDoMembro(psicopedagogo.getId()).stream()
				.anyMatch(aluno -> idUsuario.equals(aluno.getId()));
		if (!alunoVinculado) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado.");
		}

		return mensagemChatService.buscarConversa(String.valueOf(psicopedagogo.getId()), String.valueOf(idUsuario));
	}
}
