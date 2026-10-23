package br.edu.ifsp.mef.repository;

import br.edu.ifsp.mef.model.TentativaAtividade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativaAtividadeRepository extends JpaRepository<TentativaAtividade, Long> {
    boolean existsByAtividade_IdAndAluno_Id(Long atividadeId, Long alunoId);
}
