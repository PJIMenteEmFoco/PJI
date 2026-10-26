package br.edu.ifsp.mef.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import br.edu.ifsp.mef.repository.AtividadeRepository;
import br.edu.ifsp.mef.service.ConviteService;
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
	ConviteService conviteService;
	@Autowired
	EmailService emailService;
	@Autowired
	AtividadeRepository atividadeRepository;

	@GetMapping("/professor/dashboard")
	public String dashboard(Authentication authentication, Model model) {

		UsuarioDetails professor = (UsuarioDetails) authentication.getPrincipal();

		List<Atividade> atividades = professorService.buscarTodasAtividades(professor.getId());

		long quantidadeAlunos = professorService.contarAlunos(professor.getId());

		long quantidadeAtividades = professorService.contarAtividadesDaSemana(professor.getId());

		model.addAttribute("quantidadeConcluidas", atividades.stream()
				.filter(a -> a.getStatus() == br.edu.ifsp.mef.model.StatusAtividade.CONCLUIDO).count());
		model.addAttribute("quantidadeEmAndamento", atividades.stream()
				.filter(a -> a.getStatus() == br.edu.ifsp.mef.model.StatusAtividade.EM_ANDAMENTO).count());
		model.addAttribute("quantidadeNaoIniciadas", atividades.stream()
				.filter(a -> a.getStatus() == br.edu.ifsp.mef.model.StatusAtividade.NAO_ACESSADO).count());
		model.addAttribute("atividades", atividades);
		model.addAttribute("atividadesDoProfessor", atividades);
		model.addAttribute("quantidadeAlunos", quantidadeAlunos);
		model.addAttribute("quantidadeAtividades", quantidadeAtividades);
		model.addAttribute("turmas", turmaRepository.findByIdProfessor(professor.getId()));

		return "professor/dashboard_prof";
	}

	@GetMapping("/professor/tarefas")
	public String tarefas(Authentication authentication, Model model) {
		UsuarioDetails professor = (UsuarioDetails) authentication.getPrincipal();
		List<Atividade> atividades = new ArrayList<>(professorService.buscarTodasAtividades(professor.getId()));
		Set<Long> ids = new HashSet<>();
		for (Atividade a : atividades)
			ids.add(a.getId());
		for (Atividade a : atividadeRepository.findByProfessor_IdOrderByDataCriacaoDesc(professor.getId())) {
			if (ids.add(a.getId()))
				atividades.add(a);
		}
		model.addAttribute("atividades", atividades);
		model.addAttribute("professorId", professor.getId());
		return "professor/tarefas_professor";
	}

	@GetMapping("/professor/tarefas/nova")
	public String novaTarefa(Authentication authentication, Model model, @RequestParam(required = false) Long alunoId) {

		UsuarioDetails professor = (UsuarioDetails) authentication.getPrincipal();

		List<Usuario> alunos = professorService.buscarAlunosTurma(professor.getId());
		Long alunoSelecionadoId = alunoId != null && alunos.stream().anyMatch(aluno -> alunoId.equals(aluno.getId()))
				? alunoId
				: null;

		model.addAttribute("alunos", alunos);
		model.addAttribute("alunoSelecionadoId", alunoSelecionadoId);

		return "professor/criar-nova-tarefa";
	}

	@GetMapping("/professor/turmas")
	public String turmas(Authentication authentication, Model model) {

		Usuario professor = usuarioService.getByEmail(authentication.getName());

		List<Turma> turmas = turmaRepository.findByIdProfessor(professor.getId());

		List<Usuario> psicopedagogos = usuarioService.buscarPsicopedagogos();

		model.addAttribute("professorLogadoNome", professor.getNome());
		model.addAttribute("professorLogadoId", professor.getId());
		model.addAttribute("psicopedagogos", psicopedagogos);
		model.addAttribute("turmas", turmas);

		return "professor/turmas-professor";
	}

	@PostMapping("/professor/turmas/nova")
	public String criarTurma(Authentication authentication, @RequestParam String nome,
			@RequestParam(required = false) String descricao,
			@RequestParam(required = false) String emailsIntegrantes,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

		List<String> emails = Arrays.stream(Optional.ofNullable(emailsIntegrantes).orElse("").split("[\\s,;]+"))
				.map(String::trim).map(String::toLowerCase).filter(e -> !e.isEmpty()).distinct().toList();
		Professor professor = (Professor) usuarioService.getByEmail(authentication.getName());
		Turma turma = turmaService.criarTurma(nome, descricao, professor);

        int falhas = 0;
        for (String email : emails) {
            try {
                ConviteTurma convite = conviteService.criarConvite(turma.getId(), email);
                emailService.enviarConvite(email, turma.getNome(), convite.getToken());
            } catch (org.springframework.mail.MailException | IllegalArgumentException e) { falhas++; }
        }
        if (falhas > 0) redirectAttributes.addFlashAttribute("conviteErro",
            "Turma criada, mas " + falhas + " convite(s) não foram enviados. Confira os e-mails e a configuração de envio; tente novamente nesta página.");
        else if (!emails.isEmpty()) redirectAttributes.addFlashAttribute("conviteSucesso", "Convites enviados.");
        return "redirect:/professor/turmas/" + turma.getId();
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
		model.addAttribute("professorTurma",
				turma.getUsuarios().stream().filter(u -> "PROFESSOR".equals(u.getPerfil())).findFirst().orElse(null));
		model.addAttribute("psicopedagogoTurma", turma.getUsuarios().stream()
				.filter(u -> "PSICOPEDAGOGO".equals(u.getPerfil())).findFirst().orElse(null));

		return "professor/detalhes-turma";
	}

	@GetMapping("/professor/configuracoes")
	public String configuracoes(Authentication authentication, Model model) {

		Usuario professor = usuarioService.getByEmail(authentication.getName());

		model.addAttribute("professorId", professor.getId());
		model.addAttribute("professorNome", professor.getNome());
		model.addAttribute("professorTelefone", professor.getTelefone());

		return "professor/configuracoes_professor";
	}

	@GetMapping("/professor/calendario/{id}")
	public String calendarioEspecifico(@PathVariable Long id, Authentication authentication, Model model) {

		Usuario professor = usuarioService.getByEmail(authentication.getName());

		Aluno aluno = alunoService.getById(id);
		if (aluno == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado.");
		}

		boolean alunoVinculado = professorService.buscarAlunosTurma(professor.getId()).stream()
				.anyMatch(membro -> id.equals(membro.getId()));
		if (!alunoVinculado) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado.");
		}

		Calendario calendario = calendarioService.buscarOpcionalPorAluno(aluno).orElse(null);
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