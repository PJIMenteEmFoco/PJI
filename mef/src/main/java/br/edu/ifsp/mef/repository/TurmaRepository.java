package br.edu.ifsp.mef.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

	@Query("""
			SELECT COUNT(DISTINCT aluno.id)
			 FROM Turma t
			 JOIN t.usuarios professor
			 JOIN t.usuarios aluno
			 WHERE professor.id = :idProfessor
			   AND professor.perfil = 'PROFESSOR'
			   AND aluno.perfil = 'ALUNO'
			""")
	long countAlunosByIdProfessor(Long idProfessor);

	@Query("""
			    SELECT DISTINCT aluno
			    FROM Turma turma
			    JOIN turma.usuarios professor
			    JOIN turma.usuarios aluno
			    WHERE professor.id = :idProfessor
			      AND professor.perfil = 'PROFESSOR'
			      AND aluno.perfil = 'ALUNO'
			""")
	List<Usuario> buscarAlunosDoProfessor(@Param("idProfessor") Long idProfessor);

	@Query("""
			    SELECT DISTINCT professor
			    FROM Turma turma
			    JOIN turma.usuarios aluno
			    JOIN turma.usuarios professor
			    WHERE aluno.id = :idAluno
			      AND aluno.perfil = 'ALUNO'
			      AND professor.perfil = 'PROFESSOR'
			""")
	List<Usuario> buscarProfessoresDoAluno(@Param("idAluno") Long idAluno);

	@Query("""
			    SELECT DISTINCT t
			    FROM Turma t
			    JOIN t.usuarios u
			    WHERE u.id = :idProfessor
			      AND u.perfil = "PROFESSOR"
			""")
	List<Turma> findByIdProfessor(@Param("idProfessor") Long idProfessor);

	@Query("""
			    SELECT DISTINCT t
			    FROM Turma t
			    JOIN FETCH t.usuarios membro
			    JOIN t.usuarios professor
			    WHERE t.id = :idTurma
			    AND professor.id = :idProfessor
			    AND UPPER(professor.perfil) = 'PROFESSOR'
			""")
	Optional<Turma> buscarTurmaDoProfessor(@Param("idTurma") Long idTurma, @Param("idProfessor") Long idProfessor);

	// NOVO:
	// busca SOMENTE os alunos da turma selecionada
	@Query("""
			    SELECT DISTINCT usuario
			    FROM Turma turma
			    JOIN turma.usuarios usuario
			    WHERE turma.id = :idTurma
			    AND UPPER(usuario.perfil) = 'ALUNO'
			    ORDER BY usuario.nome
			""")
	List<Usuario> buscarAlunosDaTurma(@Param("idTurma") Long idTurma);
}
