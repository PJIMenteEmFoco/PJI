package br.edu.ifsp.mef.controller;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifsp.mef.model.ConviteTurma;
import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.ConviteService;
import br.edu.ifsp.mef.repository.TurmaRepository;
import br.edu.ifsp.mef.service.EmailService;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class ProfessorTurmaConviteController {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	@Autowired
	UsuarioDetailsService usuarioDetailsService;
	@Autowired
	TurmaRepository turmaRepository;
	@Autowired
	ConviteService conviteService;
	@Autowired
	EmailService emailService;

	@PostMapping("/professor/turmas/{id}/convites")
	public String convidarIntegrante(@PathVariable Long id, @RequestParam("email") String emailInformado,
			Authentication authentication, RedirectAttributes redirectAttributes) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		Usuario professor = usuarioDetailsService.getByEmail(authentication.getName());
		if (professor == null || professor.getPerfil() == null
				|| !"PROFESSOR".equalsIgnoreCase(professor.getPerfil())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas professores podem convidar integrantes.");
		}

		Turma turma = turmaRepository.buscarTurmaDoProfessor(id, professor.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada."));

		String email = emailInformado == null ? "" : emailInformado.trim().toLowerCase(Locale.ROOT);
		if (email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()) {
			redirectAttributes.addFlashAttribute("conviteErro", "Informe um endereço de e-mail válido.");
			return "redirect:/professor/turmas/" + id;
		}

        ConviteTurma convite = conviteService.criarConvite(turma.getId(), email);
        try {
            emailService.enviarConvite(email, turma.getNome(), convite.getToken());
        } catch (org.springframework.mail.MailException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("conviteErro", "Não foi possível enviar o e-mail. Verifique a configuração de e-mail e tente novamente.");
            return "redirect:/professor/turmas/" + id;
        }

		redirectAttributes.addFlashAttribute("conviteSucesso", "Convite enviado para " + email + ".");
		return "redirect:/professor/turmas/" + id;
	}
}
