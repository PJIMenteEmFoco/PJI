package br.edu.ifsp.mef.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.Atividade;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {
	
	 Optional <Atividade> findByNomeIgnoreCase (String nome);

}
