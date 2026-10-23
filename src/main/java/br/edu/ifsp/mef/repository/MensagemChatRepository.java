package br.edu.ifsp.mef.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import br.edu.ifsp.mef.model.MensagemChatEntity;

import java.util.List;

@Repository
public interface MensagemChatRepository extends JpaRepository<MensagemChatEntity, Long> {
    
    List<MensagemChatEntity> findBySenderAndRecipientOrRecipientAndSenderOrderByTimestampAsc(
        String sender1, String recipient1, String sender2, String recipient2
    );
    
    @Query("""
            SELECT m FROM MensagemChatEntity m 
            WHERE (m.sender = :user1 AND m.recipient = :user2) 
               OR (m.sender = :user2 AND m.recipient = :user1) 
            ORDER BY m.timestamp ASC
        """)
        List<MensagemChatEntity> findConversasBetween(@Param("user1") String user1, @Param("user2") String user2);
}
