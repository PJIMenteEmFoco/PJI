package br.edu.ifsp.mef.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.ifsp.mef.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional <Usuario> findByEmailIgnoreCase(String email);
	
	List<Usuario> findByPerfil(String perfil);

}
