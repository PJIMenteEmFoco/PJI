package br.edu.ifsp.mef.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifsp.mef.model.MensagemChatEntity;

import java.util.List;

@Repository
public interface MensagemChatRepository extends JpaRepository<MensagemChatEntity, Long> {
    
    List<MensagemChatEntity> findBySenderAndRecipientOrRecipientAndSenderOrderByTimestampAsc(
        String sender1, String recipient1, String sender2, String recipient2
    );
}
