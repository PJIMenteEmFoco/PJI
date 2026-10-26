package br.edu.ifsp.mef.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.TurmaRepository;
import br.edu.ifsp.mef.service.ProfessorService;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class ProfessorAlunosController {

	private final UsuarioDetailsService usuarioService;
	private final ProfessorService professorService;
	private final TurmaRepository turmaRepository;

	public ProfessorAlunosController(UsuarioDetailsService usuarioService, ProfessorService professorService,
			TurmaRepository turmaRepository) {
		this.usuarioService = usuarioService;
		this.professorService = professorService;
		this.turmaRepository = turmaRepository;
	}

	@GetMapping("/professor/alunos")
	@Transactional
	public String alunos(Authentication authentication, Model model) {
		Usuario professor = usuarioService.getByEmail(authentication.getName());
		Long professorId = professor.getId();
		List<Usuario> vinculados = professorService.buscarAlunosTurma(professorId);
		List<Turma> turmas = turmaRepository.findByIdProfessor(professorId);

		Map<Long, AlunoCard> cards = new LinkedHashMap<>();
		for (Usuario aluno : vinculados) {
			cards.putIfAbsent(aluno.getId(), new AlunoCard(aluno.getId(), aluno.getNome(), aluno.getEmail()));
		}

		for (Turma turma : turmas) {
			if (turma.getUsuarios() == null)
				continue;
			for (Usuario integrante : turma.getUsuarios()) {
				AlunoCard card = cards.get(integrante.getId());
				if (card != null && "ALUNO".equalsIgnoreCase(String.valueOf(integrante.getPerfil()))) {
					card.turmas.add(new TurmaCard(turma.getId(), turma.getNome()));
				}
			}
		}

		model.addAttribute("alunosCards", new ArrayList<>(cards.values()));
		model.addAttribute("turmas",
				turmas.stream().map(t -> new TurmaCard(t.getId(), t.getNome())).collect(Collectors.toList()));
		return "professor/alunos";
	}

	public static final class TurmaCard {
		private final Long id;
		private final String nome;

		public TurmaCard(Long id, String nome) {
			this.id = id;
			this.nome = nome;
		}

		public Long getId() {
			return id;
		}

		public String getNome() {
			return nome;
		}
	}

	public static final class AlunoCard {
		private final Long id;
		private final String nome;
		private final String email;
		private final List<TurmaCard> turmas = new ArrayList<>();

		public AlunoCard(Long id, String nome, String email) {
			this.id = id;
			this.nome = nome;
			this.email = email;
		}

		public Long getId() {
			return id;
		}

		public String getNome() {
			return nome;
		}

		public String getEmail() {
			return email;
		}

		public List<TurmaCard> getTurmas() {
			return turmas;
		}

		public String getTextoBusca() {
			return nome + " " + (email == null ? "" : email) + " "
					+ turmas.stream().map(TurmaCard::getNome).collect(Collectors.joining(" "));
		}

		public String getIdsTurmas() {
			return turmas.stream().map(t -> t.getId().toString()).collect(Collectors.joining("|"));
		}
	}
}
