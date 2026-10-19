package br.edu.ifsp.mef.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.Calendario;
import br.edu.ifsp.mef.model.ConviteTurma;
import br.edu.ifsp.mef.model.Professor;
import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.ConviteTurmaRepository;
import br.edu.ifsp.mef.repository.TurmaRepository;
import br.edu.ifsp.mef.service.AlunoService;
import br.edu.ifsp.mef.service.AtividadeService;
import br.edu.ifsp.mef.service.CalendarioService;
import br.edu.ifsp.mef.service.EmailService;
import br.edu.ifsp.mef.service.ProfessorService;
import br.edu.ifsp.mef.service.TurmaService;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class ProfessorViewController {

	@Autowired
	ProfessorService professorService;
	@Autowired
	UsuarioDetailsService usuarioService;
	@Autowired
	TurmaRepository turmaRepository;
	@Autowired
	AlunoService alunoService;
	@Autowired
	CalendarioService calendarioService;
	@Autowired
	AtividadeService atividadeService;
	@Autowired
	TurmaService turmaService;
	@Autowired
	ConviteTurmaRepository conviteTurmaRepository;
	@Autowired
	EmailService emailService;

	@GetMapping("/professor/dashboard")
	public String dashboard(Authentication authentication, Model model) {

		UsuarioDetails professor = (UsuarioDetails) authentication.getPrincipal();

		List<Atividade> atividades = professorService.buscarTodasAtividades(professor.getId());

		long quantidadeAlunos = professorService.contarAlunos(professor.getId());

		long quantidadeAtividades = professorService.contarAtividadesDaSemana(professor.getId());

		model.addAttribute("atividades", atividades);
		model.addAttribute("quantidadeAlunos", quantidadeAlunos);
		model.addAttribute("quantidadeAtividades", quantidadeAtividades);

		return "professor/dashboard_prof";
	}

	@GetMapping("/professor/tarefas")
	public String tarefas(Authentication authentication, Model model) {

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

		List<Usuario> psicopedagogos = usuarioService.buscarPsicopedagogos();

		model.addAttribute("psicopedagogos", psicopedagogos);
		model.addAttribute("turmas", turmas);

		return "professor/turmas-professor";
	}

	@PostMapping("/professor/turmas/nova")
	public String criarTurma(Authentication authentication, @RequestParam String nome, @RequestParam(required = false) String descricao,
			@RequestParam(required = false) String emailsIntegrantes) {

		List<String> emails = Arrays.stream(Optional.ofNullable(emailsIntegrantes).orElse("").split("[\\s,;]+"))
				.map(String::trim).map(String::toLowerCase).filter(e -> !e.isEmpty()).distinct().toList();
		Professor professor = (Professor) usuarioService.getByEmail(authentication.getName());
		Turma turma = turmaService.criarTurma(nome, descricao, professor);

		
		if (!emails.isEmpty()) {
			for (String email : emails) {
				ConviteTurma convite = new ConviteTurma();
				convite.setEmail(email);
				convite.setIdTurma(turma.getId());
				convite.setToken(UUID.randomUUID().toString());
				convite.setExpiracao(LocalDateTime.now().plusDays(7));
				conviteTurmaRepository.save(convite);

				emailService.enviarConvite(email, turma.getNome(), convite.getToken());
			}
		}
		return "redirect:/professor/turmas";
	}

	@GetMapping("/professor/turmas/{id}")
	public String detalhesTurma(@PathVariable Long id, Authentication authentication, Model model) {

		Usuario professor = usuarioService.getByEmail(authentication.getName());
		Turma turma = turmaRepository.findByIdProfessor(professor.getId()).stream().filter(t -> t.getId().equals(id))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));
		List<Usuario> alunos = professorService.buscarAlunosPorTurma(id);
		long totalAtividades = professorService.contarAtividadesDaTurma(id);
		model.addAttribute("turma", turma);
		model.addAttribute("alunos", alunos);
		model.addAttribute("totalAtividades", totalAtividades);

		return "professor/detalhes-turma";
	}

	@GetMapping("/professor/configuracoes")
	public String configuracoes() {
		return "professor/configuracoes_professor";
	}

	@GetMapping("/professor/calendario")
	public String calendario() {
		return "professor/calendario_prof";
	}

	@GetMapping("/professor/calendario/{id}")
	public String calendarioEspecifico(@PathVariable Long id, Model model) {

		Aluno aluno = alunoService.getById(id);
		Calendario calendario = calendarioService.buscarPorAluno(aluno);
		List<Atividade> atividadesDoAluno = alunoService.buscarAtividades(id);

		model.addAttribute("aluno", aluno);
		model.addAttribute("calendario", calendario);
		model.addAttribute("atividadesDoAluno", atividadesDoAluno);

		return "professor/agenda_aluno";
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