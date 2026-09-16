package br.edu.ifsp.mef.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.TurmaRepository;
import br.edu.ifsp.mef.service.ProfessorService;
import br.edu.ifsp.mef.service.TurmaService;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

/**
 * Serve as telas do perfil PROFESSOR.
 * Por enquanto os metodos so retornam a view (sem dados dinamicos) --
 * a ligacao com dados reais (turmas, atividades, etc.) ainda precisa ser feita.
 *
 * OBS: nao existe rota para "chat_professor" pois o HTML/CSS dessa pagina
 * veio vazio (0 bytes) no zip enviado.
 */
@Controller
public class ProfessorViewController {

	@Autowired
	ProfessorService professorService;
	@Autowired
	UsuarioDetailsService usuarioService;
	@Autowired
	TurmaRepository turmaRepository;
	
	@GetMapping("/professor/dashboard")
	public String dashboard(Authentication authentication, Model model) {
		UsuarioDetails professor = (UsuarioDetails) authentication.getPrincipal();

	    long quantidadeAlunos = professorService.contarAlunos(professor.getId());
	    long quantidadeAtividades = professorService.contarAtividadesDaSemana(professor.getId());
	    model.addAttribute("quantidadeAlunos", quantidadeAlunos);
	    model.addAttribute("quantidadeAtividades", quantidadeAtividades);
		return "professor/dashboard_prof";
	}

	@GetMapping("/professor/tarefas")
	public String tarefas() {
		return "professor/tarefas_professor";
	}

	@GetMapping("/professor/tarefas/nova")
	public String novaTarefa() {
		return "professor/criar-nova-tarefa";
	}

	@GetMapping("/professor/turmas")
	public String turmas(Authentication authentication, Model model) {
		Usuario professor = usuarioService.getByEmail(authentication.getName());
	    List<Turma> turmas = turmaRepository.findByIdProfessor(professor.getId());
	    model.addAttribute("turmas", turmas);

		return "professor/turmas-professor";
	}

	@GetMapping("/professor/turma/{id}")
	public String detalhesTurma(@PathVariable Long id, Model model) {
		model.addAttribute("idTurma", id);
		return "professor/detalhes-turma";
	}

	@GetMapping("/professor/configuracoes")
	public String configuracoes() {
		return "professor/configuracoes_professor";
	}
	
	@GetMapping("/professor/chat")
	public String chat(Authentication authentication, Model model) {
		UsuarioDetails professor = (UsuarioDetails) authentication.getPrincipal();
		List<Usuario> alunos = professorService.buscarAlunosTurma(professor.getId());
	    model.addAttribute("usuarioLogadoId", professor.getId());
	    model.addAttribute("alunos", alunos);
	    model.addAttribute("usuarioLogado", professor);
		return "professor/chat_prof";
	}
}
