package br.edu.ifsp.mef.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.edu.ifsp.mef.service.EmailService;

@Controller
public class EmailController {

	@Autowired
	EmailService emailService;

	@GetMapping("/email-recuperacao")
	public String emailRecuperacao(@RequestParam("email") String email, @RequestParam("nome") String nome) {
		emailService.enviarCodigoRecuperacao(email, emailService.gerarCodigo(), nome);
		return "redirect:/";
	}
	
	@PostMapping("/verificar")
	public String verificar(@RequestParam String codigo, String email) {
		emailService.verificarCodigo(codigo, email);
	    return "redirect:/login";
	}
}
