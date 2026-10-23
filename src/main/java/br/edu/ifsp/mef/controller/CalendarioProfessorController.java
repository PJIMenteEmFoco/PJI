package br.edu.ifsp.mef.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import br.edu.ifsp.mef.model.EventoProfessor;
import br.edu.ifsp.mef.model.Feriado;
import br.edu.ifsp.mef.model.PrioridadeEvento;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.service.EventoProfessorService;
import br.edu.ifsp.mef.service.FeriadoService;
import br.edu.ifsp.mef.service.ProfessorService;

@Controller
public class CalendarioProfessorController {

	@Autowired
	ProfessorService professorService;
	@Autowired
	EventoProfessorService eventoService;
	@Autowired
	FeriadoService feriadoService;

	private Long idDe(Authentication authentication) {
		return ((UsuarioDetails) authentication.getPrincipal()).getId();
	}

	@GetMapping("/professor/calendario")
	public String calendario(Authentication authentication, Model model) {
		Long professorId = idDe(authentication);

		int anoAtual = LocalDate.now().getYear();

		List<Feriado> feriados = new ArrayList<>();

		for (int ano = anoAtual - 1; ano <= anoAtual + 1; ano++) {

			feriados.addAll(feriadoService.buscarOuSincronizarAno(ano));
		}

		model.addAttribute("feriados", feriados);
		model.addAttribute("atividades", professorService.buscarTodasAtividades(professorId));
		model.addAttribute("eventos", eventoService.listarDoProfessor(professorId));
		model.addAttribute("quantidadeAvaliar", 0L);

		return "professor/calendario_prof";
	}

	@PostMapping("/professor/calendario/eventos")
	@ResponseBody
	public ResponseEntity<?> criarEvento(Authentication authentication, @RequestParam String titulo,
			@RequestParam LocalDate dataEvento, @RequestParam LocalTime horario,
			@RequestParam PrioridadeEvento prioridade, @RequestParam String descricao) {

		try {
			EventoProfessor evento = eventoService.criar(idDe(authentication), titulo, dataEvento, horario, prioridade,
					descricao);
			return ResponseEntity.ok(Map.of("id", evento.getId()));

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
		}
	}
}