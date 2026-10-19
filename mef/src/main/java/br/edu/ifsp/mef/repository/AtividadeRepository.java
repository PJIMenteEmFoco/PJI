package br.edu.ifsp.mef.repository;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifsp.mef.model.Atividade;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

	Optional<Atividade> findByNomeIgnoreCase(String nome);

	@Query("""
			    SELECT DISTINCT a
			    FROM Atividade a
			    JOIN a.alunos aluno
			    JOIN aluno.turmas turma
			    JOIN turma.usuarios professor
			    WHERE professor.id = :idProfessor
			      AND professor.perfil = 'PROFESSOR'
			      AND a.prazo BETWEEN :inicioSemana AND :fimSemana
			    ORDER BY a.prazo ASC
			""")
	List<Atividade> findAtividadesDaSemanaDoProfessor(@Param("idProfessor") Long idProfessor,
			@Param("inicioSemana") LocalDate inicioSemana, @Param("fimSemana") LocalDate fimSemana);

	@Query("""
			    SELECT COUNT(DISTINCT a.id)
			    FROM Atividade a
			    JOIN a.alunos aluno
			    JOIN aluno.turmas turma
			    JOIN turma.usuarios professor
			    WHERE professor.id = :idProfessor
			      AND professor.perfil = 'PROFESSOR'
			      AND a.prazo BETWEEN :inicioSemana AND :fimSemana
			""")
	long countAtividadesDaSemanaDoProfessor(@Param("idProfessor") Long idProfessor,
			@Param("inicioSemana") LocalDate inicioSemana, @Param("fimSemana") LocalDate fimSemana);

	@Query("""
			    SELECT DISTINCT a
			    FROM Atividade a
			    JOIN a.alunos aluno
			    JOIN aluno.turmas turma
			    JOIN turma.usuarios professor
			    WHERE professor.id = :idProfessor
			      AND professor.perfil = 'PROFESSOR'
			    ORDER BY a.prazo ASC
			""")
	List<Atividade> findAtividadesDoProfessor(@Param("idProfessor") Long idProfessor);

	@Query("""
			    SELECT DISTINCT a
			    FROM Atividade a
			    JOIN a.alunos aluno
			    WHERE aluno.id = :idAluno
			    ORDER BY a.prazo ASC
			""")
	List<Atividade> findAtividadesDoAluno(@Param("idAluno") Long idAluno);

	@Query("""
			    SELECT COUNT(DISTINCT a.id)
			    FROM Atividade a
			    JOIN a.alunos aluno
			    JOIN aluno.turmas turma
			    WHERE turma.id = :idTurma
			""")
	long countAtividadesDaTurma(@Param("idTurma") Long idTurma);

	@Query("""
			select a from Atividade a join a.alunos al
			where al.id = :alunoId
			  and a.professor.id = :professorId
			  and a.dataCriacao >= :desde
			order by a.dataCriacao desc, a.id desc
			""")
	List<Atividade> ultimasDoAluno(@Param("alunoId") Long alunoId, @Param("professorId") Long professorId,
			@Param("desde") LocalDateTime desde, Pageable pageable);
}
