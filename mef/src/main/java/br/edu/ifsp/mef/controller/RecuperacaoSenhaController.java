package br.edu.ifsp.mef.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serve as telas do fluxo de "esqueci minha senha".
 *
 * IMPORTANTE: apenas as VIEWS estao ligadas aqui. Os formularios dessas 3
 * paginas ainda usam apenas JS de front-end (nao enviam nada pro backend).
 * O EmailController ja existe e cobre parte do fluxo (GET /email-recuperacao
 * e POST /verificar), mas nao ha ainda um endpoint para de fato trocar a
 * senha depois do codigo verificado -- isso precisa ser desenhado e
 * implementado antes de conectar esses forms de verdade.
 */
@Controller
public class RecuperacaoSenhaController {

	@GetMapping("/email-verificacao")
	public String emailVerificacao() {
		return "email-verificacao";
	}

	@GetMapping("/codigo-verificacao")
	public String codigoVerificacao() {
		return "codigo-verificacao";
	}

	@GetMapping("/nova-senha")
	public String novaSenha() {
		return "nova-senha";
	}
}
