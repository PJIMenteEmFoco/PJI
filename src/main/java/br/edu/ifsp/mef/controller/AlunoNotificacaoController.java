package br.edu.ifsp.mef.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class AlunoNotificacaoController {

	private final UsuarioDetailsService usuarios;

	public AlunoNotificacaoController(UsuarioDetailsService usuarios) {
		this.usuarios = usuarios;
	}

	@GetMapping("/aluno/notificacoes")
	public String paginaAluno(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para acessar as notificações.");
		}

		Usuario usuario = usuarios.getByEmail(authentication.getName());
		if (usuario == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão não encontrada.");
		}
		if (!"ALUNO".equalsIgnoreCase(usuario.getPerfil())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"A página de notificações é exclusiva para alunos.");
		}

		return "aluno/notificacoes";
	}
}
