package br.edu.ifsp.mef.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifsp.mef.model.TentativaAtividade;

public interface TentativaAtividadeRepository extends JpaRepository<TentativaAtividade, Long> {

	boolean existsByAtividade_IdAndAluno_Id(Long atividadeId, Long alunoId);

	List<TentativaAtividade> findByAluno_IdInOrderByFinalizadaEmDescIdDesc(Collection<Long> alunoIds);

	@Query("SELECT t FROM TentativaAtividade t JOIN FETCH t.atividade WHERE t.id = :tentativaId AND t.aluno.id = :alunoId")
	Optional<TentativaAtividade> buscarTentativaDoAluno(@Param("tentativaId") Long tentativaId,
			@Param("alunoId") Long alunoId);
}
