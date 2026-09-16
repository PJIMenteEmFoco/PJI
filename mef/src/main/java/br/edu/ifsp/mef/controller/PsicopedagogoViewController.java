package br.edu.ifsp.mef.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serve as telas do perfil PSICOPEDAGOGO.
 *
 * OBS: nao existe rota para "dashboard_psic" pois o HTML/CSS dessa pagina
 * veio vazio (0 bytes) no zip enviado. Quando o arquivo estiver pronto, basta
 * adicionar um metodo igual aos das outras dashboards.
 */
@Controller
public class PsicopedagogoViewController {

	@GetMapping("/psicopedagogo/tarefas")
	public String tarefas() {
		return "psicopedagogo/tarefas_psicopedagogo";
	}
}
