package br.edu.ifsp.mef.controller;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.TipoAtividade;
import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.TurmaRepository;
import br.edu.ifsp.mef.service.AlunoService;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class PsicopedagogoAlunosController {

	private static final DateTimeFormatter DATA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@Autowired
	UsuarioDetailsService usuarioService;
	@Autowired
	TurmaRepository turmaRepository;
	@Autowired
	AlunoService alunoService;

	@GetMapping("/psicopedagogo/alunos")
	@Transactional
	public String alunos(Authentication authentication, Model model) {
		Usuario psicopedagogo = psicopedagogoAutenticado(authentication);
		List<Turma> turmasDoPsicopedagogo = turmaRepository.buscarTurmasPorMembro(psicopedagogo.getId());
		List<Usuario> alunosVinculados = turmaRepository.buscarAlunosDoMembro(psicopedagogo.getId());

		Map<Long, AlunoCard> cardsPorId = new LinkedHashMap<>();
		for (Usuario aluno : alunosVinculados) {
			if (aluno != null && aluno.getId() != null && "ALUNO".equalsIgnoreCase(String.valueOf(aluno.getPerfil()))) {
				cardsPorId.putIfAbsent(aluno.getId(), new AlunoCard(aluno.getId(), aluno.getNome(), aluno.getEmail()));
			}
		}

		List<TurmaOpcao> opcoesTurma = turmasDoPsicopedagogo.stream().filter(Objects::nonNull)
				.map(turma -> new TurmaOpcao(turma.getId(), turma.getNome()))
				.sorted(Comparator.comparing(TurmaOpcao::getNome, String.CASE_INSENSITIVE_ORDER)).toList();

		for (Turma turma : turmasDoPsicopedagogo) {
			if (turma.getUsuarios() == null) {
				continue;
			}
			for (Usuario membro : turma.getUsuarios()) {
				if (membro == null || membro.getId() == null
						|| !"ALUNO".equalsIgnoreCase(String.valueOf(membro.getPerfil()))) {
					continue;
				}
				AlunoCard card = cardsPorId.get(membro.getId());
				if (card != null && card.turmas.stream().noneMatch(t -> Objects.equals(t.id, turma.getId()))) {
					card.turmas.add(new TurmaOpcao(turma.getId(), turma.getNome()));
				}
			}
		}

		List<AlunoCard> cards = new ArrayList<>(cardsPorId.values());
		cards.forEach(AlunoCard::atualizarTextoBusca);
		cards.sort(Comparator.comparing(AlunoCard::getNomeSeguro, String.CASE_INSENSITIVE_ORDER));

		model.addAttribute("alunosCards", cards);
		model.addAttribute("turmas", opcoesTurma);
		model.addAttribute("quantidadeAlunos", cards.size());
		return "psicopedagogo/alunos";
	}

	@GetMapping("/psicopedagogo/alunos/{id}/ficha")
	@Transactional
	public String fichaAluno(@PathVariable Long id, Authentication authentication, Model model) {
		Usuario psicopedagogo = psicopedagogoAutenticado(authentication);
		List<Turma> turmasDoPsicopedagogo = turmaRepository.buscarTurmasPorMembro(psicopedagogo.getId());

		List<String> turmasAluno = turmasDoPsicopedagogo.stream()
				.filter(turma -> turma.getUsuarios() != null && turma.getUsuarios().stream()
						.anyMatch(membro -> Objects.equals(membro.getId(), id)
								&& "ALUNO".equalsIgnoreCase(String.valueOf(membro.getPerfil()))))
				.map(Turma::getNome).filter(Objects::nonNull).distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();

		if (turmasAluno.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado nas suas turmas.");
		}

		Aluno aluno = alunoService.getById(id);
		if (aluno == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado.");
		}

		List<AtividadeCard> atividades = alunoService.buscarAtividades(id).stream()
				.sorted(Comparator.comparing(Atividade::getPrazo, Comparator.nullsLast(Comparator.naturalOrder())))
				.map(this::paraAtividadeCard).toList();

		model.addAttribute("aluno", aluno);
		model.addAttribute("turmasAluno", turmasAluno);
		model.addAttribute("turmasTexto", String.join(" • ", turmasAluno));
		model.addAttribute("quantidadeTurmas", turmasAluno.size());
		model.addAttribute("atividades", atividades);
		model.addAttribute("quantidadeAtividades", atividades.size());
		model.addAttribute("dataNascimentoTexto",
				aluno.getDataNascimento() == null ? "Não informado" : aluno.getDataNascimento().format(DATA_FORMATO));
		model.addAttribute("tempoFocoTexto",
				aluno.getTempoFoco() > 0 ? formatarNumero(aluno.getTempoFoco(), 2) : "Não informado");
		model.addAttribute("xpTexto", aluno.getXp() == null ? "0" : formatarNumero(aluno.getXp(), 0));
		model.addAttribute("nivelNome", aluno.getNivel() == null || aluno.getNivel().getNome() == null ? "Não definido"
				: aluno.getNivel().getNome());

		return "psicopedagogo/ficha-aluno";
	}

	private Usuario psicopedagogoAutenticado(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "É necessário entrar novamente.");
		}
		Usuario usuario = usuarioService.getByEmail(authentication.getName());
		if (usuario == null || !"PSICOPEDAGOGO".equalsIgnoreCase(String.valueOf(usuario.getPerfil()))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas psicopedagogos podem acessar esta página.");
		}
		return usuario;
	}

	private AtividadeCard paraAtividadeCard(Atividade atividade) {
		return new AtividadeCard(atividade.getId(),
				atividade.getNome() == null || atividade.getNome().isBlank() ? "Atividade sem título"
						: atividade.getNome(),
				rotuloTipo(atividade.getTipoAtividade()),
				atividade.getPrazo() == null ? "Sem prazo" : atividade.getPrazo().format(DATA_FORMATO),
				atividade.getDescricao());
	}

	private String rotuloTipo(TipoAtividade tipo) {
		if (tipo == null)
			return "Tipo não informado";
		return switch (tipo) {
		case JOGO_DA_MEMORIA -> "Jogo da Memória";
		case QUIZ -> "Quiz";
		case CRUZADINHA -> "Palavras Cruzadas";
		case LEITURA_GUIADA -> "Leitura Guiada";
		};
	}

	private String formatarNumero(Number valor, int maximoDecimais) {
		NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
		formato.setMaximumFractionDigits(maximoDecimais);
		formato.setMinimumFractionDigits(0);
		return formato.format(valor);
	}

	public static final class TurmaOpcao {
		private final Long id;
		private final String nome;

		public TurmaOpcao(Long id, String nome) {
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
		private final List<TurmaOpcao> turmas = new ArrayList<>();
		private String textoBusca;

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

		public List<TurmaOpcao> getTurmas() {
			return turmas;
		}

		public String getIdsTurmas() {
			return turmas.stream().map(t -> String.valueOf(t.getId())).collect(Collectors.joining("|"));
		}

		public String getTextoBusca() {
			return textoBusca;
		}

		public String getNomeSeguro() {
			return nome == null ? "" : nome;
		}

		private void atualizarTextoBusca() {
			this.textoBusca = (nome == null ? "" : nome) + " " + (email == null ? "" : email) + " " + turmas.stream()
					.map(TurmaOpcao::getNome).filter(Objects::nonNull).collect(Collectors.joining(" "));
		}
	}

	public static final class AtividadeCard {
		private final Long id;
		private final String nome;
		private final String tipo;
		private final String prazo;
		private final String descricao;

		public AtividadeCard(Long id, String nome, String tipo, String prazo, String descricao) {
			this.id = id;
			this.nome = nome;
			this.tipo = tipo;
			this.prazo = prazo;
			this.descricao = descricao;
		}

		public Long getId() {
			return id;
		}

		public String getNome() {
			return nome;
		}

		public String getTipo() {
			return tipo;
		}

		public String getPrazo() {
			return prazo;
		}

		public String getDescricao() {
			return descricao;
		}
	}
}
