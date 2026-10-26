package br.edu.ifsp.mef.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.mef.model.ConviteTurma;
import br.edu.ifsp.mef.repository.ConviteTurmaRepository;

import java.time.LocalDateTime;

@Service
public class ConviteService {

	@Autowired
    ConviteTurmaRepository conviteRepository;

    @Transactional
    public ConviteTurma criarConvite(Long idTurmas) {
        LocalDateTime expiracao = LocalDateTime.now().plusHours(3);
        ConviteTurma convite = new ConviteTurma();
        convite.setIdTurma(idTurmas);
        convite.setExpiracao(expiracao);
        return conviteRepository.save(convite);
    }

    public ConviteTurma validarToken(Long id) {
        ConviteTurma convite = conviteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Link de convite inválido ou não encontrado."));

        if (convite.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Este link de convite expirou.");
        }
        return convite;
    }

    @Transactional
    public void removerConvite(Long id) {
        conviteRepository.deleteById(id);
    }
}