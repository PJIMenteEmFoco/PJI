package br.edu.ifsp.mef.controller;

import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.TipoAtividade;
import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.AlunoRepository;
import br.edu.ifsp.mef.repository.TurmaRepository;
import br.edu.ifsp.mef.service.AlunoService;
import br.edu.ifsp.mef.service.ProfessorService;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class ProfessorFichaAlunoController {

	private static final DateTimeFormatter DATA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter PRAZO_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final UsuarioDetailsService usuarioService;
	private final ProfessorService professorService;
	private final AlunoService alunoService;
	private final AlunoRepository alunoRepository;
	private final TurmaRepository turmaRepository;

	public ProfessorFichaAlunoController(UsuarioDetailsService usuarioService, ProfessorService professorService,
			AlunoService alunoService, AlunoRepository alunoRepository, TurmaRepository turmaRepository) {
		this.usuarioService = usuarioService;
		this.professorService = professorService;
		this.alunoService = alunoService;
		this.alunoRepository = alunoRepository;
		this.turmaRepository = turmaRepository;
	}

	@GetMapping("/professor/alunos/{id}/ficha")
	@Transactional
	public String fichaAluno(@PathVariable Long id, Authentication authentication, Model model) {

		Usuario professor = professorAutenticado(authentication);
		Aluno aluno = alunoAutorizado(id, professor.getId());

		List<String> turmasAluno = turmaRepository.buscarTurmasPorMembro(id).stream()
				.filter(turma -> turma.getUsuarios() != null && turma.getUsuarios().stream()
						.anyMatch(membro -> Objects.equals(membro.getId(), professor.getId())
								&& "PROFESSOR".equalsIgnoreCase(String.valueOf(membro.getPerfil()))))
				.map(Turma::getNome).filter(Objects::nonNull).distinct().sorted().toList();

		List<AtividadeFicha> atividades = alunoService.buscarAtividades(id).stream()
				.filter(atividade -> atividade.getProfessor() != null
						&& Objects.equals(atividade.getProfessor().getId(), professor.getId()))
				.sorted(Comparator.comparing(Atividade::getPrazo, Comparator.nullsLast(Comparator.naturalOrder())))
				.map(this::paraFicha).toList();

		model.addAttribute("aluno", aluno);
		model.addAttribute("turmasAluno", turmasAluno);
		model.addAttribute("turmasTexto",
				turmasAluno.isEmpty() ? "Sem turma identificada" : String.join(" • ", turmasAluno));
		model.addAttribute("atividades", atividades);
		model.addAttribute("quantidadeAtividades", atividades.size());
		model.addAttribute("quantidadeTurmas", turmasAluno.size());
		model.addAttribute("dataNascimentoTexto",
				aluno.getDataNascimento() == null ? "Não informado" : aluno.getDataNascimento().format(DATA_FORMATO));
		model.addAttribute("tempoFocoTexto",
				aluno.getTempoFoco() > 0 ? formatarNumero(aluno.getTempoFoco(), 2) : "Não informado");
		model.addAttribute("xpTexto", aluno.getXp() == null ? "0" : formatarNumero(aluno.getXp(), 0));
		model.addAttribute("nivelNome", aluno.getNivel() == null || aluno.getNivel().getNome() == null ? "Não definido"
				: aluno.getNivel().getNome());

		return "professor/ficha-aluno";
	}

	@PostMapping(value = "/professor/alunos/{id}/ficha/dados", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Transactional
	public ResponseEntity<Map<String, Object>> atualizarFicha(@PathVariable Long id, Authentication authentication,
			@RequestBody AtualizarFichaRequest request) {

		Usuario professor = professorAutenticado(authentication);
		Aluno aluno = alunoAutorizado(id, professor.getId());

		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os dados enviados estão vazios.");
		}

		String nome = limpar(request.getNome());
		if (nome == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do aluno é obrigatório.");
		}
		validarTamanho(nome, 120, "O nome");
		validarTamanho(request.getTelefone(), 40, "O telefone");
		validarTamanho(request.getHiperfoco(), 1500, "O hiperfoco");
		validarTamanho(request.getDesregulacao(), 2000, "A informação sobre desregulação");
		validarTamanho(request.getLaudo(), 3000, "O laudo");

		Float tempoFoco = request.getTempoFoco();
		if (tempoFoco != null && (!Float.isFinite(tempoFoco) || tempoFoco < 0 || tempoFoco > 1440)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"O tempo de foco deve ser um número entre 0 e 1440.");
		}

		aluno.setNome(nome);
		aluno.setTelefone(limpar(request.getTelefone()));
		aluno.setHiperfoco(limpar(request.getHiperfoco()));
		aluno.setDesregulacao(limpar(request.getDesregulacao()));
		aluno.setLaudo(limpar(request.getLaudo()));
		if (tempoFoco != null) {
			aluno.setTempoFoco(tempoFoco);
		}

		Aluno salvo = alunoRepository.save(aluno);

		Map<String, Object> dados = new LinkedHashMap<>();
		dados.put("id", salvo.getId());
		dados.put("nome", salvo.getNome());
		dados.put("telefone", salvo.getTelefone());
		dados.put("hiperfoco", salvo.getHiperfoco());
		dados.put("tempoFoco", salvo.getTempoFoco());
		dados.put("desregulacao", salvo.getDesregulacao());
		dados.put("laudo", salvo.getLaudo());

		Map<String, Object> resposta = new LinkedHashMap<>();
		resposta.put("sucesso", true);
		resposta.put("mensagem", "Ficha do aluno atualizada com sucesso.");
		resposta.put("aluno", dados);

		return ResponseEntity.ok(resposta);
	}

	private Usuario professorAutenticado(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "É necessário entrar novamente.");
		}

		Usuario usuario = usuarioService.getByEmail(authentication.getName());
		if (usuario == null || !"PROFESSOR".equalsIgnoreCase(String.valueOf(usuario.getPerfil()))) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas professores podem acessar esta ficha.");
		}
		return usuario;
	}

	private Aluno alunoAutorizado(Long alunoId, Long professorId) {
		boolean vinculado = professorService.buscarAlunosTurma(professorId).stream()
				.anyMatch(usuario -> Objects.equals(usuario.getId(), alunoId)
						&& "ALUNO".equalsIgnoreCase(String.valueOf(usuario.getPerfil())));

		if (!vinculado) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado nas suas turmas.");
		}

		Aluno aluno = alunoService.getById(alunoId);
		if (aluno == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado.");
		}
		return aluno;
	}

	private AtividadeFicha paraFicha(Atividade atividade) {
		String tipo = rotuloTipo(atividade.getTipoAtividade());
		String prazo = atividade.getPrazo() == null ? "Sem prazo" : atividade.getPrazo().format(PRAZO_FORMATO);
		return new AtividadeFicha(atividade.getId(),
				atividade.getNome() == null ? "Atividade sem título" : atividade.getNome(), tipo, prazo,
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

	private String limpar(String valor) {
		if (valor == null)
			return null;
		String limpo = valor.trim();
		return limpo.isEmpty() ? null : limpo;
	}

	private void validarTamanho(String valor, int maximo, String campo) {
		if (valor != null && valor.length() > maximo) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					campo + " ultrapassa o limite de " + maximo + " caracteres.");
		}
	}

	public static final class AtividadeFicha {
		private final Long id;
		private final String nome;
		private final String tipo;
		private final String prazo;
		private final String descricao;

		public AtividadeFicha(Long id, String nome, String tipo, String prazo, String descricao) {
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

	public static final class AtualizarFichaRequest {
		private String nome;
		private String telefone;
		private String hiperfoco;
		private Float tempoFoco;
		private String desregulacao;
		private String laudo;

		public AtualizarFichaRequest() {
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public String getTelefone() {
			return telefone;
		}

		public void setTelefone(String telefone) {
			this.telefone = telefone;
		}

		public String getHiperfoco() {
			return hiperfoco;
		}

		public void setHiperfoco(String hiperfoco) {
			this.hiperfoco = hiperfoco;
		}

		public Float getTempoFoco() {
			return tempoFoco;
		}

		public void setTempoFoco(Float tempoFoco) {
			this.tempoFoco = tempoFoco;
		}

		public String getDesregulacao() {
			return desregulacao;
		}

		public void setDesregulacao(String desregulacao) {
			this.desregulacao = desregulacao;
		}

		public String getLaudo() {
			return laudo;
		}

		public void setLaudo(String laudo) {
			this.laudo = laudo;
		}
	}
}
