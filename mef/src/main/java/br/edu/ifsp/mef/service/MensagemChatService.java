package br.edu.ifsp.mef.service;

import br.edu.ifsp.mef.model.MensagemChat;
import br.edu.ifsp.mef.model.MensagemChatEntity;
import br.edu.ifsp.mef.repository.MensagemChatRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MensagemChatService {

	@Autowired
	MensagemChatRepository repository;

	public MensagemChatEntity salvar(MensagemChat mensagem) {

		MensagemChatEntity entity = new MensagemChatEntity();

		entity.setSender(mensagem.getSender());
		entity.setRecipient(mensagem.getRecipient());
		entity.setContent(mensagem.getContent());
		entity.setTimestamp(LocalDateTime.now());

		return repository.save(entity);
	}

	public List<MensagemChatEntity> buscarConversa(String usuario1, String usuario2) {

		return repository.findBySenderAndRecipientOrRecipientAndSenderOrderByTimestampAsc(usuario1, usuario2, usuario2,
				usuario1);
	}
}
