package br.edu.ifsp.mef.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifsp.mef.model.ConfiguracoesUsuario;

public interface ConfiguracoesUsuarioRepository extends JpaRepository<ConfiguracoesUsuario, Long> {
}
