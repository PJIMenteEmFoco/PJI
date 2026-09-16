package br.edu.ifsp.mef.repository;

import java.util.List;

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
		    SELECT DISTINCT t
		    FROM Turma t
		    JOIN t.usuarios u
		    WHERE u.id = :idProfessor
		      AND u.perfil = "PROFESSOR"
		""")
		List<Turma> findByIdProfessor(@Param("idProfessor") Long idProfessor);


}
