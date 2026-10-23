package br.edu.ifsp.mef.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.edu.ifsp.mef.service.AlunoService;
import br.edu.ifsp.mef.service.EmailService;
import br.edu.ifsp.mef.service.TurmaService;

@Controller
@RequestMapping(value = "/email")
public class EmailController {

	@Autowired
	EmailService emailService;
	@Autowired
	TurmaService turmaService;
	@Autowired
	AlunoService alunoService;

	@GetMapping("/recuperacao")
	public String emailRecuperacao(@RequestParam("email") String email, @RequestParam("nome") String nome) {
		emailService.enviarCodigoRecuperacao(email, emailService.gerarCodigo(), nome);
		return "redirect:/";
	}
	
	@PostMapping("/verificar")
	public String verificar(@RequestParam String codigo, String email) {
		emailService.verificarCodigo(codigo, email);
	    return "redirect:/login";
	}
	
	@GetMapping("/notificacaoAtividade")
	public void notificarNovaAtividade(@RequestParam("email") String email) {
		emailService.envioNotificacaoAtividade(email);
	}
	
	@GetMapping("/notificacaoConviteTurma")
	public void notificarConviteTurma(@RequestParam("email") String email, Long idTurma) {
		String codigoTurma = turmaService.buscarPorId(idTurma).getCodigoTurma();
		String nomeAluno = alunoService.getByEmail(email).getNome();
		emailService.envioNotificacaoIngressoTurma(email, codigoTurma, nomeAluno);
	}
}
