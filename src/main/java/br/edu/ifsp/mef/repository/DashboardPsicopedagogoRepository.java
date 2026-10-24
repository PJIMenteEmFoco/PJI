package br.edu.ifsp.mef.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.StatusAtividade;
import br.edu.ifsp.mef.model.TipoAtividade;

public interface DashboardPsicopedagogoRepository extends JpaRepository<Atividade, Long> {


    @Query("""
            SELECT DISTINCT a.id, a.nome, a.prazo, aluno.id, aluno.nome
            FROM Atividade a
            JOIN a.alunos aluno
            WHERE aluno.id IN :alunoIds
              AND a.tipoAtividade IN :tipos
              AND (a.status IS NULL OR a.status <> :naoVinculado)
            ORDER BY a.prazo ASC
            """)
    List<Object[]> buscarAtribuicoes(
            @Param("alunoIds") List<Long> alunoIds,
            @Param("tipos") List<TipoAtividade> tipos,
            @Param("naoVinculado") StatusAtividade naoVinculado);

  
    default List<Object[]> buscarAtribuicoes(List<Long> alunoIds) {
        return buscarAtribuicoes(
                alunoIds,
                List.of(TipoAtividade.JOGO_DA_MEMORIA, TipoAtividade.QUIZ, TipoAtividade.CRUZADINHA),
                StatusAtividade.NAO_VINCULADO);
    }

    
    @Query("""
            SELECT DISTINCT tentativa.atividade.id, tentativa.aluno.id
            FROM TentativaAtividade tentativa
            WHERE tentativa.aluno.id IN :alunoIds
            """)
    List<Object[]> buscarParesConcluidos(@Param("alunoIds") List<Long> alunoIds);

    @Query("""
            SELECT tentativa.aluno.nome, tentativa.atividade.nome, tentativa.finalizadaEm
            FROM TentativaAtividade tentativa
            WHERE tentativa.aluno.id IN :alunoIds
            ORDER BY tentativa.finalizadaEm DESC
            """)
    List<Object[]> buscarTentativaMaisRecente(
            @Param("alunoIds") List<Long> alunoIds,
            Pageable pageable);


    default List<Object[]> buscarTentativaMaisRecente(List<Long> alunoIds) {
        return buscarTentativaMaisRecente(alunoIds, PageRequest.of(0, 1));
    }
}
