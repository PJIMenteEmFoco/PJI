package br.edu.ifsp.mef.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.Aluno;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

}
