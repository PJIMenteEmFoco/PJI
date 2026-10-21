package br.edu.ifsp.mef.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.EventoProfessor;

public interface EventoProfessorRepository extends JpaRepository<EventoProfessor, Long> {

	List<EventoProfessor> findByProfessorIdOrderByDataEventoAscHorarioAsc(Long professorId);
}