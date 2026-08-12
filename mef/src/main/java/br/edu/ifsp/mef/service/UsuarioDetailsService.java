package br.edu.ifsp.mef.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Professor;
import br.edu.ifsp.mef.model.Psicopedagogo;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {
	
	@Autowired
	UsuarioRepository usuarioRepository;
	
	@Autowired
	PasswordEncoder encoder;
	
	UsuarioDetails usuarioDetails;
	
	public void cadastrarUsuario(Usuario usuario, String perfil) {

	    Optional<Usuario> usuarioExistente =
	            usuarioRepository.findByEmailIgnoreCase(usuario.getEmail());

	    if (usuarioExistente.isPresent()) {
	        return;
	    }

	    Usuario novoUsuario;

	    switch (perfil) {
	        case "ALUNO":
	            novoUsuario = new Aluno();
	            break;

	        case "PROFESSOR":
	            novoUsuario = new Professor();
	            break;

	        case "PSICOPEDAGOGO":
	            novoUsuario = new Psicopedagogo();
	            break;

	        default:
	            throw new IllegalArgumentException("Perfil inválido");
	    }

	    novoUsuario.setNome(usuario.getNome());
	    novoUsuario.setEmail(usuario.getEmail());
	    novoUsuario.setSenha(encoder.encode(usuario.getSenha()));
	    novoUsuario.setTelefone(usuario.getTelefone());
	    novoUsuario.setDataNasci(usuario.getDataNasci());
	    novoUsuario.setPerfil(perfil);
	    novoUsuario.setAtivado(true);

	    usuarioRepository.save(novoUsuario);
	}
	
	    public boolean alterarStatusUsuario(Long id) {
	    	try {
	    		Usuario user = getById(id);
	    		if (user == null) {
	    			return false;
	    		}
	    		user.setAtivado(!user.isAtivado());
	    		usuarioRepository.save(user);	
	    		return true;
	    		} catch (Exception e) {
	    			return false;
	    		}
	    	}
	    
		public Usuario getById(long id) {
			Optional<Usuario> byId = usuarioRepository.findById(id);
			if(byId.isEmpty()) {
				return null;
			}
			return byId.get();
	}
	
	public boolean atualizarNome(long id, String nome) {
		Usuario user = getById(id);
		if (user == null) {
			return false;
		}
		user.setNome(nome);
		usuarioRepository.save(user);
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		UsuarioDetails details = (UsuarioDetails) authentication.getPrincipal();
		details.getUsuario().setNome(nome);
		return true;
	}
	
	public boolean atualizarSenha(long id, String senha) {
		Usuario user = getById(id);
		if (user == null) {
			return false;
		}
		user.setSenha(encoder.encode(senha));
		usuarioRepository.save(user);

		return true;
	}
	
	public boolean atualizarTelefone(long id, String telefone) {
		Usuario user = getById(id);
		if (user == null) {
			return false;
		}
		user.setTelefone((telefone));
		usuarioRepository.save(user);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		UsuarioDetails details = (UsuarioDetails) authentication.getPrincipal();
		details.getUsuario().setTelefone(telefone);

		return true;
	}


	    @Override
	    public UserDetails loadUserByUsername(String username)
	            throws UsernameNotFoundException {

	    	 Usuario usuario = usuarioRepository
	    	            .findByEmailIgnoreCase(username)
	    	            .orElseThrow(() -> new UsernameNotFoundException(
	    	                    "Usuário não encontrado"
	    	            ));

	    	 return new UsuarioDetails(usuario);
	    }
}	    
