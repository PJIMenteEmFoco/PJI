package br.edu.ifsp.mef.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.RecuperacaoSenhaService;
import br.edu.ifsp.mef.repository.UsuarioRepository;

@Controller
public class RecuperacaoSenhaController {

	private static final String EMAIL_SESSAO = "mefRecuperacaoEmail";
	private static final String ORIGEM_SESSAO = "mefRecuperacaoOrigem";
	private static final String PERFIL_SESSAO = "mefRecuperacaoPerfil";
	private static final String USUARIO_VALIDADO_SESSAO = "mefSenhaUsuarioValidado";
	private static final String VALIDACAO_EM_SESSAO = "mefSenhaValidadaEm";
	private static final long JANELA_REDEFINICAO_MS = 10 * 60 * 1000L;

	@Autowired
	RecuperacaoSenhaService recuperacaoService;
	@Autowired
	UsuarioRepository usuarioRepository;

	@GetMapping("/email-verificacao")
	public String emailVerificacao(@RequestParam(value = "origem", required = false) String origem,
			@RequestParam(value = "perfil", required = false) String perfil,
			@RequestParam(value = "erro", required = false) String erro, Authentication authentication,
			HttpSession session, Model model) {
		String origemEfetiva = origem;
		String perfilEfetivo = perfil;
		if ((origemEfetiva == null || origemEfetiva.isBlank())
				&& ORIGEM_AJUSTES.equals(session.getAttribute(ORIGEM_SESSAO))) {
			origemEfetiva = ORIGEM_AJUSTES;
			perfilEfetivo = (String) session.getAttribute(PERFIL_SESSAO);
		}

		boolean fluxoAjustes = ORIGEM_AJUSTES.equals(origemEfetiva);
		Usuario usuarioAutenticado = usuarioAutenticado(authentication);
		String emailPreenchido = (String) session.getAttribute(EMAIL_SESSAO);

		if (fluxoAjustes) {
			String slug = perfilSlug(perfilEfetivo);
			if (usuarioAutenticado == null || slug == null
					|| !slug.equals(perfilSlug(usuarioAutenticado.getPerfil()))) {
				return "redirect:/login";
			}
			emailPreenchido = usuarioAutenticado.getEmail();
		}

		model.addAttribute("origem", fluxoAjustes ? ORIGEM_AJUSTES : "");
		model.addAttribute("perfil", fluxoAjustes ? perfilSlug(usuarioAutenticado.getPerfil()) : "");
		model.addAttribute("emailPreenchido", emailPreenchido == null ? "" : emailPreenchido);
		model.addAttribute("emailSomenteLeitura", fluxoAjustes);
		model.addAttribute("erroMensagem",
				"conta".equals(erro) ? "Não foi possível continuar com esses dados. Confira o e-mail e tente novamente."
						: null);
		return "email-verificacao";
	}

	@PostMapping("/email-verificacao")
	public String solicitarCodigo(@RequestParam("email") String email,
			@RequestParam(value = "origem", required = false) String origem,
			@RequestParam(value = "perfil", required = false) String perfil, Authentication authentication,
			HttpSession session) {
		if (email == null || email.isBlank())
			return "redirect:/email-verificacao?erro=conta";

		String emailNormalizado = email.trim();
		boolean fluxoAjustes = ORIGEM_AJUSTES.equals(origem);
		String perfilEfetivo = "";

		if (fluxoAjustes) {
			Usuario usuario = usuarioAutenticado(authentication);
			String slugInformado = perfilSlug(perfil);
			if (usuario == null || slugInformado == null || !slugInformado.equals(perfilSlug(usuario.getPerfil()))
					|| !usuario.getEmail().equalsIgnoreCase(emailNormalizado)) {
				return "redirect:/email-verificacao?erro=conta";
			}
			perfilEfetivo = slugInformado;
			emailNormalizado = usuario.getEmail();
		}

		limparSessaoRedefinicao(session);
		session.setAttribute(EMAIL_SESSAO, emailNormalizado);
		session.setAttribute(ORIGEM_SESSAO, fluxoAjustes ? ORIGEM_AJUSTES : "");
		session.setAttribute(PERFIL_SESSAO, perfilEfetivo);

		// A mesma resposta é usada quando o endereço não existe, para evitar
		// revelar quais e-mails estão cadastrados.
		recuperacaoService.solicitarCodigo(emailNormalizado);
		return "redirect:/codigo-verificacao?enviado";
	}

	@GetMapping("/codigo-verificacao")
	public String codigoVerificacao(@RequestParam(value = "enviado", required = false) String enviado,
			@RequestParam(value = "erro", required = false) String erro, HttpSession session, Model model) {
		if (!(session.getAttribute(EMAIL_SESSAO) instanceof String email) || email.isBlank()) {
			return redirecionarParaEmail(session);
		}
		model.addAttribute("origem", valorSessao(session, ORIGEM_SESSAO));
		model.addAttribute("perfil", valorSessao(session, PERFIL_SESSAO));
		model.addAttribute("codigoEnviado", enviado != null);
		model.addAttribute("erroCodigo", erro != null);
		return "codigo-verificacao";
	}

	@PostMapping("/codigo-verificacao")
	public String validarCodigo(@RequestParam("codigo") String codigo, HttpSession session) {
		String email = (String) session.getAttribute(EMAIL_SESSAO);
		if (email == null || email.isBlank())
			return redirecionarParaEmail(session);

		if (!recuperacaoService.verificarCodigo(email, codigo)) {
			return "redirect:/codigo-verificacao?erro";
		}

		Usuario usuario = recuperacaoService.buscarUsuario(email).orElse(null);
		if (usuario == null)
			return "redirect:/codigo-verificacao?erro";

		session.setAttribute(USUARIO_VALIDADO_SESSAO, usuario.getId());
		session.setAttribute(VALIDACAO_EM_SESSAO, System.currentTimeMillis());
		String origem = valorSessao(session, ORIGEM_SESSAO);
		String perfil = valorSessao(session, PERFIL_SESSAO);
		if (ORIGEM_AJUSTES.equals(origem)) {
			return "redirect:/nova-senha?origem=ajustes&perfil=" + perfil;
		}
		return "redirect:/nova-senha";
	}

	@GetMapping("/nova-senha")
	public String novaSenha(@RequestParam(value = "erro", required = false) String erro, HttpSession session,
			Model model) {
		if (!sessaoRedefinicaoValida(session)) {
			return redirecionarParaEmail(session);
		}
		model.addAttribute("origem", valorSessao(session, ORIGEM_SESSAO));
		model.addAttribute("perfil", valorSessao(session, PERFIL_SESSAO));
		model.addAttribute("erroMensagem", mensagemErro(erro));
		return "nova-senha";
	}

	@PostMapping("/nova-senha")
	public String trocarSenha(@RequestParam("novaSenha") String novaSenha,
			@RequestParam("confirmarSenha") String confirmarSenha, Authentication authentication, HttpSession session) {
		if (!sessaoRedefinicaoValida(session))
			return redirecionarParaEmail(session);

		Long usuarioId = usuarioIdValidado(session);
		String origem = valorSessao(session, ORIGEM_SESSAO);
		String perfil = valorSessao(session, PERFIL_SESSAO);
		if (usuarioId == null)
			return redirecionarParaEmail(session);

		if (ORIGEM_AJUSTES.equals(origem)) {
			Usuario usuarioAutenticado = usuarioAutenticado(authentication);
			if (usuarioAutenticado == null || !usuarioId.equals(usuarioAutenticado.getId())
					|| !perfil.equals(perfilSlug(usuarioAutenticado.getPerfil()))) {
				limparSessaoRedefinicao(session);
				return "redirect:/login";
			}
		}

		if (!novaSenha.equals(confirmarSenha)) {
			return redirecionarNovaSenha("confirmacao", origem, perfil);
		}
		if (!recuperacaoService.senhaAtendePolitica(novaSenha)) {
			return redirecionarNovaSenha("senha", origem, perfil);
		}
		if (!recuperacaoService.alterarSenha(usuarioId, novaSenha)) {
			return redirecionarNovaSenha("geral", origem, perfil);
		}

		limparSessaoRedefinicao(session);
		if (ORIGEM_AJUSTES.equals(origem)) {
			return switch (perfil) {
			case "aluno" -> "redirect:/aluno/configuracoes";
			case "professor" -> "redirect:/professor/configuracoes";
			case "psicopedagogo" -> "redirect:/psicopedagogo/configuracoes";
			default -> "redirect:/login";
			};
		}
		return "redirect:/login?senhaAlterada";
	}

	private String redirecionarNovaSenha(String erro, String origem, String perfil) {
		if (ORIGEM_AJUSTES.equals(origem)) {
			return "redirect:/nova-senha?erro=" + erro + "&origem=ajustes&perfil=" + perfil;
		}
		return "redirect:/nova-senha?erro=" + erro;
	}

	private String mensagemErro(String erro) {
		if (erro == null)
			return null;
		return switch (erro) {
		case "confirmacao" -> "As senhas não coincidem. Confira os dois campos.";
		case "senha" -> "Use ao menos 8 caracteres, com letra maiúscula, letra minúscula, número e caractere especial.";
		case "geral" -> "Não foi possível alterar a senha. Tente novamente.";
		default -> null;
		};
	}

	private boolean sessaoRedefinicaoValida(HttpSession session) {
		Long usuarioId = usuarioIdValidado(session);
		Object validadaEm = session.getAttribute(VALIDACAO_EM_SESSAO);
		if (usuarioId == null || !(validadaEm instanceof Long instante))
			return false;
		long idade = System.currentTimeMillis() - instante;
		if (idade < 0 || idade > JANELA_REDEFINICAO_MS) {
			limparAutorizacaoRedefinicao(session);
			return false;
		}
		return true;
	}

	private Long usuarioIdValidado(HttpSession session) {
		Object id = session.getAttribute(USUARIO_VALIDADO_SESSAO);
		return id instanceof Long value ? value : null;
	}

	private void limparAutorizacaoRedefinicao(HttpSession session) {
		session.removeAttribute(USUARIO_VALIDADO_SESSAO);
		session.removeAttribute(VALIDACAO_EM_SESSAO);
	}

	private void limparSessaoRedefinicao(HttpSession session) {
		limparAutorizacaoRedefinicao(session);
		session.removeAttribute(EMAIL_SESSAO);
		session.removeAttribute(ORIGEM_SESSAO);
		session.removeAttribute(PERFIL_SESSAO);
	}

	private String redirecionarParaEmail(HttpSession session) {
		if (ORIGEM_AJUSTES.equals(session.getAttribute(ORIGEM_SESSAO))) {
			return "redirect:/email-verificacao?origem=ajustes&perfil=" + valorSessao(session, PERFIL_SESSAO);
		}
		return "redirect:/email-verificacao";
	}

	private String valorSessao(HttpSession session, String chave) {
		Object valor = session.getAttribute(chave);
		return valor instanceof String texto ? texto : "";
	}

	private Usuario usuarioAutenticado(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken)
			return null;
		return usuarioRepository.findByEmailIgnoreCase(authentication.getName()).orElse(null);
	}

	private String perfilSlug(String perfil) {
		if (perfil == null)
			return null;
		return switch (perfil.trim().toUpperCase()) {
		case "ALUNO" -> "aluno";
		case "PROFESSOR" -> "professor";
		case "PSICOPEDAGOGO" -> "psicopedagogo";
		default -> null;
		};
	}

	private static final String ORIGEM_AJUSTES = "ajustes";
}
