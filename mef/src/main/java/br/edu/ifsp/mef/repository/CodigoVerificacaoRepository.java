package br.edu.ifsp.mef.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.ifsp.mef.model.CodigoVerificacao;
import br.edu.ifsp.mef.model.Usuario;

public interface CodigoVerificacaoRepository extends JpaRepository<CodigoVerificacao, Long> {

	Optional <CodigoVerificacao> findByUsuario(Usuario usuario);
}
