package br.edu.ifsp.mef.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Calendario;
import br.edu.ifsp.mef.model.Evento;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.CalendarioService;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class CalendarioController {

	@Autowired
	CalendarioService calendarioService;
	UsuarioDetailsService usuarioService;

	@GetMapping("/calendario/{id}")
	public String visualizarCalendario(@PathVariable Long id, Model model) {
		Usuario aluno = usuarioService.getById(id);
		if (aluno.getPerfil()=="ALUNO") {
			Calendario calendario = calendarioService.buscarPorAluno((Aluno) aluno);
			model.addAttribute("calendario", calendario);
		}
		return "calendario";
	}

	@PostMapping("/criar")
	public String criarCalendario(@ModelAttribute Aluno aluno) {
		calendarioService.criarCalendario(aluno);
		return "redirect:/calendario";
	}

	@PostMapping("/{idCalendario}/evento")
	public String adicionarEvento(@PathVariable Long idCalendario, @ModelAttribute Evento evento, Long id) {

		calendarioService.adicionarEvento(idCalendario, evento);

		return "redirect:/calendario/" + id;
	}

	@PostMapping("/evento/{idEvento}/atualizar")
	public String atualizarEvento(@PathVariable Long idEvento, @ModelAttribute Evento evento) {

		Evento eventoAtualizado = calendarioService.atualizarEvento(idEvento, evento);

		return "redirect:/calendario/" + eventoAtualizado.getCalendario().getAluno().getId();
	}

	@PostMapping("/evento/{idEvento}/remover")
	public String removerEvento(@PathVariable Long idEvento) {
		calendarioService.removerEvento(idEvento);
		return "redirect:/calendario";
	}

	@PostMapping("/{idCalendario}/atividade/{atividadeId}")
	public String adicionarAtividade(@PathVariable Long idCalendario, @PathVariable Long idAtividade) {
		calendarioService.adicionarAtividade(idCalendario, idAtividade);
		return "redirect:/calendario/" + calendarioService.buscarPorId(idAtividade).getAluno().getId();
	}

	@PostMapping("/{calendarioId}/atividade/{atividadeId}/remover")
	public String removerAtividade(@PathVariable Long calendarioId, @PathVariable Long atividadeId) {
		calendarioService.removerAtividade(calendarioId, atividadeId);
		return "redirect:/calendario/" + calendarioId;
	}
}
