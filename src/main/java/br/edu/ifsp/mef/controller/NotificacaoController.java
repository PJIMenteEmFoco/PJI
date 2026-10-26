package br.edu.ifsp.mef.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.mef.model.Notificacao;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.NotificacaoService;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class NotificacaoController {
	@Autowired
	NotificacaoService notificacaoService;
	@Autowired
	UsuarioDetailsService usuarios;

	public NotificacaoController(NotificacaoService notificacaoService, UsuarioDetailsService usuarios) {
		this.notificacaoService = notificacaoService;
		this.usuarios = usuarios;
	}

	@GetMapping("/professor/notificacoes")
	public String paginaProfessor(Authentication authentication, Model model) {
		Usuario usuario = usuarioAutenticado(authentication);
		exigirPerfilProfessor(usuario);
		model.addAttribute("usuarioNome", usuario.getNome());
		return "professor/notificacoes";
	}

	@GetMapping(value = "/api/notificacoes", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> listar(Authentication authentication) {
		Usuario usuario = usuarioAutenticado(authentication);
		List<NotificacaoDTO> lista = notificacaoService.listar(usuario.getId()).stream().map(NotificacaoDTO::de)
				.toList();
		boolean receber = notificacaoService.receberNotificacoes(usuario);
		long naoLidas = lista.stream().filter(n -> !n.lida()).count();
		return Map.of("notificacoes", lista, "receberNotificacoes", receber, "naoLidas", naoLidas);
	}

	@PatchMapping(value = "/api/notificacoes/{id}/lida", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> marcarComoLida(@PathVariable Long id, Authentication authentication) {
		Usuario usuario = usuarioAutenticado(authentication);
		notificacaoService.marcarComoLida(id, usuario.getId());
		return Map.of("sucesso", true);
	}

	@PatchMapping(value = "/api/notificacoes/lidas", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> marcarTodasComoLidas(Authentication authentication) {
		Usuario usuario = usuarioAutenticado(authentication);
		int alteradas = notificacaoService.marcarTodasComoLidas(usuario.getId());
		return Map.of("sucesso", true, "alteradas", alteradas);
	}

	@PatchMapping(value = "/api/notificacoes/preferencia", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> atualizarPreferencia(@RequestBody PreferenciaRequest request,
			Authentication authentication) {
		Usuario usuario = usuarioAutenticado(authentication);
		if (request == null || request.receber() == null)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe se deseja receber notificações.");
		boolean valor = notificacaoService.atualizarPreferencia(usuario.getId(), request.receber());
		return Map.of("sucesso", true, "receberNotificacoes", valor);
	}

	private Usuario usuarioAutenticado(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para acessar as notificações.");
		}
		Usuario usuario = usuarios.getByEmail(authentication.getName());
		if (usuario == null)
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão não encontrada.");
		return usuario;
	}

	private void exigirPerfilProfessor(Usuario usuario) {
		if (!"PROFESSOR".equalsIgnoreCase(usuario.getPerfil()))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A página é exclusiva para professores.");
	}

	public record PreferenciaRequest(Boolean receber) {
	}

	public record NotificacaoDTO(Long id, String titulo, String mensagem, String link, boolean lida,
			LocalDateTime dataCriacao) {
		static NotificacaoDTO de(Notificacao n) {
			return new NotificacaoDTO(n.getId(), n.getTitulo(), n.getMensagem(), n.getLink(), n.isLida(),
					n.getDataCriacao());
		}
	}
}
