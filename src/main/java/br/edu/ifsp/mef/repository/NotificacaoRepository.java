package br.edu.ifsp.mef.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
	
    List<Notificacao> findAllByDestinatario_IdOrderByDataCriacaoDesc(Long destinatarioId);
    
    Optional<Notificacao> findByIdAndDestinatario_Id(Long id, Long destinatarioId);
    
    long countByDestinatario_IdAndLidaFalse(Long destinatarioId);
}
