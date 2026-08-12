package br.edu.ifsp.mef.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.Instituicao;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
}