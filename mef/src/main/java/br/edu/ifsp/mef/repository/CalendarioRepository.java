package br.edu.ifsp.mef.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Calendario;

public interface CalendarioRepository extends JpaRepository<Calendario, Long> {
	
	Optional<Calendario> findByAluno(Aluno aluno);
}
