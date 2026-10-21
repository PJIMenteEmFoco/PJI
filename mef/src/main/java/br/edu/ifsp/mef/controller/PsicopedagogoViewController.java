package br.edu.ifsp.mef.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PsicopedagogoViewController {

	@GetMapping("/psicopedagogo/tarefas")
	public String tarefas() {
		return "psicopedagogo/tarefas_psicopedagogo";
	}
	
	@GetMapping("/psicopedagogo/dashboard")
	public String dashboard() {
		return "professor/dashboard_psico";
	}
}
