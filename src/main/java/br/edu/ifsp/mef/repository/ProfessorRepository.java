package br.edu.ifsp.mef.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.Professor;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

}
