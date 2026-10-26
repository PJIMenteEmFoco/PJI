package br.edu.ifsp.mef.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import br.edu.ifsp.mef.model.ConviteTurma;
public interface ConviteTurmaRepository extends JpaRepository<ConviteTurma, Long> {
    Optional<ConviteTurma> findByToken(String token);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ConviteTurma c where c.token = :token")
    Optional<ConviteTurma> buscarParaAceitar(@Param("token") String token);
}
