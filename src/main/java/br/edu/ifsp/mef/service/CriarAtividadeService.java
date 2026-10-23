package br.edu.ifsp.mef.service;

import br.edu.ifsp.mef.model.CriarAtividadeRequest;
import br.edu.ifsp.mef.model.CriarAtividadeRequest.PerguntaInput;
import br.edu.ifsp.mef.model.CriarAtividadeRequest.ParInput;
import br.edu.ifsp.mef.model.CriarAtividadeRequest.PalavraInput;
import br.edu.ifsp.mef.model.*;
import br.edu.ifsp.mef.repository.AtividadeRepository;
import jakarta.persistence.EntityManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CriarAtividadeService {

	public record Resultado(Long id, String status, String mensagem) {
	}

	@Autowired
	AtividadeRepository atividadeRepository;
	@Autowired
	UsuarioDetailsService usuarioService;
	@Autowired
	ProfessorService professorService;
	@Autowired
	EntityManager em;
	@Autowired
	NotificacaoService notificacaoService;

	@Value("${mef.leitura.upload-dir:uploads/leitura}")
	private String pastaPdf;

	public CriarAtividadeService(AtividadeRepository atividadeRepository, UsuarioDetailsService usuarioService,
			ProfessorService professorService, EntityManager em, NotificacaoService notificacaoService) {
		this.atividadeRepository = atividadeRepository;
		this.usuarioService = usuarioService;
		this.professorService = professorService;
		this.em = em;
		this.notificacaoService = notificacaoService;
	}

	@Transactional
	public Resultado salvar(CriarAtividadeRequest req, String emailAutenticado) {
		return gravar(req, emailAutenticado, null);
	}

	@Transactional
	public Resultado atualizarRascunho(Long id, CriarAtividadeRequest req, String emailAutenticado) {
		return gravar(req, emailAutenticado, id);
	}

	private Resultado gravar(CriarAtividadeRequest req, String emailAutenticado, Long idParaEditar) {
		if (req == null)
			throw new IllegalArgumentException("Envie os dados da atividade.");

		Usuario usuario = usuarioService.getByEmail(emailAutenticado);
		if (!(usuario instanceof Professor professor)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente professores podem criar atividades.");
		}

		boolean publicar;
		if ("Ativa".equalsIgnoreCase(req.status()))
			publicar = true;
		else if ("Rascunho".equalsIgnoreCase(req.status()))
			publicar = false;
		else
			throw new IllegalArgumentException("Status inválido: use Rascunho ou Ativa.");

		TipoAtividade tipo = switch (texto(req.tipo()).toLowerCase(Locale.ROOT)) {
		case "quiz" -> TipoAtividade.QUIZ;
		case "memoria" -> TipoAtividade.JOGO_DA_MEMORIA;
		case "cruzadas" -> TipoAtividade.CRUZADINHA;
		case "leitura" -> TipoAtividade.LEITURA_GUIADA;
		default -> throw new IllegalArgumentException("Tipo de atividade inválido.");
		};

		String nome = limitado(req.titulo(), 255, "O título", true);
		String descricao = limitado(req.instrucoes(), 255, "As instruções", false);
		if (publicar && req.prazo() == null)
			throw new IllegalArgumentException("Informe o prazo de entrega.");
		if (publicar && req.prazo().isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("O prazo não pode estar no passado.");
		}
		if (publicar && tipo == TipoAtividade.LEITURA_GUIADA) {
			throw new IllegalArgumentException(
					"Leitura Guiada ainda requer upload e processamento do PDF com IA. Salve como rascunho por enquanto.");
		}

		Set<Long> ids = new HashSet<>(req.alunos() == null ? List.of() : req.alunos());
		if (ids.size() > 500 || ids.contains(null))
			throw new IllegalArgumentException("Lista de alunos inválida.");
		if (publicar && ids.isEmpty())
			throw new IllegalArgumentException("Selecione pelo menos um aluno.");

		if (!ids.isEmpty()) {
			Set<Long> permitidos = new HashSet<>();
			for (Usuario aluno : professorService.buscarAlunosTurma(professor.getId())) {
				permitidos.add(aluno.getId());
			}
			if (!permitidos.containsAll(ids)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"Você só pode enviar atividades aos alunos das suas turmas.");
			}
		}

		switch (tipo) {
		case QUIZ -> validarQuiz(req.perguntas(), publicar);
		case JOGO_DA_MEMORIA -> validarMemoria(req.pares(), publicar);
		case CRUZADINHA -> validarPalavras(req.palavras(), publicar);
		case LEITURA_GUIADA -> validarLeitura(req.trechos());
		}

		Atividade atividade;
		String pdfAnterior = null;
		if (idParaEditar == null) {
			atividade = new Atividade();
		} else {
			atividade = atividadeRepository.findById(idParaEditar)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rascunho não encontrado."));
			if (atividade.getProfessor() == null || !atividade.getProfessor().getId().equals(professor.getId())) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode editar essa atividade.");
			}
			if (atividade.getStatus() != StatusAtividade.NAO_VINCULADO) {
				throw new IllegalArgumentException(
						"A edição desta rota é exclusiva de rascunhos ainda não publicados.");
			}
			if (atividade.getTipoAtividade() == TipoAtividade.LEITURA_GUIADA) {
				LeituraGuiada anterior = em.find(LeituraGuiada.class, idParaEditar);
				if (anterior != null)
					pdfAnterior = anterior.getArquivoPdf();
			}

			removerConteudo(atividade);
		}
		atividade.setNome(nome);
		atividade.setDescricao(descricao);
		atividade.setPrazo(req.prazo());
		atividade.setTipoAtividade(tipo);
		atividade.setProfessor(professor);
		atividade.setStatus(publicar ? StatusAtividade.NAO_ACESSADO : StatusAtividade.NAO_VINCULADO);

		atividade.getDestinatariosRascunho().clear();
		if (!publicar)
			atividade.getDestinatariosRascunho().addAll(ids);
		atividade.getAlunos().clear();
		if (publicar)
			for (Long id : ids) {
				Aluno aluno = em.find(Aluno.class, id);
				if (aluno == null)
					throw new IllegalArgumentException("Aluno não encontrado: " + id);
				atividade.getAlunos().add(aluno);
			}

		atividadeRepository.saveAndFlush(atividade);
		switch (tipo) {
		case QUIZ -> salvarQuiz(atividade, req.perguntas());
		case JOGO_DA_MEMORIA -> salvarMemoria(atividade, req.pares());
		case CRUZADINHA -> salvarCruzadinha(atividade, req.palavras());
		case LEITURA_GUIADA -> salvarLeitura(atividade, req.trechos(), pdfAnterior);
		}
		em.flush();
		if (publicar)
			notificacaoService.notificarNovaAtividade(atividade);
		return new Resultado(atividade.getId(), publicar ? "Ativa" : "Rascunho",
				publicar ? "Atividade publicada e atribuída aos alunos."
						: "Rascunho salvo. Você pode reabri-lo para editar ou publicar.");
	}

	private void salvarQuiz(Atividade a, List<PerguntaInput> itens) {
		Quiz quiz = new Quiz();
		quiz.setAtividade(a);
		if (itens != null)
			for (PerguntaInput item : itens) {
				if (item == null)
					continue;
				Pergunta pergunta = new Pergunta();
				pergunta.setEnunciado(texto(item.pergunta()));
				List<String> opcoes = item.opcoes() == null ? List.of() : item.opcoes();
				for (int i = 0; i < opcoes.size(); i++) {
					switch (i) {
					case 0 -> pergunta.setAlternativa1(texto(opcoes.get(i)));
					case 1 -> pergunta.setAlternativa2(texto(opcoes.get(i)));
					case 2 -> pergunta.setAlternativa3(texto(opcoes.get(i)));
					case 3 -> pergunta.setAlternativa4(texto(opcoes.get(i)));
					case 4 -> pergunta.setAlternativa5(texto(opcoes.get(i)));
					default -> throw new IllegalArgumentException("Máximo de 5 alternativas por pergunta.");
					}
				}
				int indice = item.correta() == null ? -1 : item.correta();
				pergunta.setAlternativaCorreta(indice >= 0 && indice < opcoes.size() ? indice + 1 : 0);
				quiz.addPergunta(pergunta);
			}
		em.persist(quiz);
	}

	private void salvarMemoria(Atividade a, List<ParInput> itens) {
		JogoMemoria jogo = new JogoMemoria();
		jogo.setAtividade(a);
		if (itens != null)
			for (ParInput par : itens) {
				if (par != null)
					jogo.addPar(new ParCarta(texto(par.termo()), texto(par.significado())));
			}
		em.persist(jogo); // cascade=ALL grava ParCarta
	}

	private void salvarCruzadinha(Atividade a, List<PalavraInput> itens) {
		Cruzadinha cruzadinha = new Cruzadinha();
		cruzadinha.setAtividade(a);
		List<MontadorCruzadinha.Entrada> palavras = new ArrayList<>();
		if (itens != null)
			for (PalavraInput item : itens) {
				if (item == null)
					continue;
				String palavra = normalizarPalavra(item.palavra());
				if (!palavra.isEmpty())
					palavras.add(new MontadorCruzadinha.Entrada(palavra, texto(item.dica())));
			}
		if (palavras.size() >= 2 && palavras.stream().allMatch(i -> i.palavra().matches("[A-Z]{2,16}"))) {
			try {
				MontadorCruzadinha.Resultado grade = MontadorCruzadinha.montar(palavras);
				cruzadinha.setLinhas(grade.linhas());
				cruzadinha.setColunas(grade.colunas());
				for (MontadorCruzadinha.Posicao pos : grade.palavras()) {
					PalavraCruzada p = new PalavraCruzada();
					p.setResposta(pos.palavra());
					p.setDica(pos.dica());
					p.setLinha(pos.linha());
					p.setColuna(pos.coluna());
					p.setDirecao(pos.direcao());
					cruzadinha.addPalavra(p);
				}
			} catch (IllegalArgumentException ex) {
				if (a.getStatus() != StatusAtividade.NAO_VINCULADO)
					throw ex;
				posicionarSemCruzamentos(cruzadinha, palavras); // Permite rascunho incompleto.
			}
		} else {
			if (a.getStatus() != StatusAtividade.NAO_VINCULADO) {
				throw new IllegalArgumentException(
						"Para publicar, informe ao menos duas palavras válidas com cruzamentos.");
			}
			posicionarSemCruzamentos(cruzadinha, palavras);
		}
		em.persist(cruzadinha);
	}

	private void posicionarSemCruzamentos(Cruzadinha grade, List<MontadorCruzadinha.Entrada> entradas) {
		int maior = 0;
		for (MontadorCruzadinha.Entrada e : entradas) {
			PalavraCruzada p = new PalavraCruzada();
			p.setResposta(e.palavra());
			p.setDica(e.dica());
			p.setLinha(grade.getPalavraCruzada().size() * 2);
			p.setColuna(0);
			p.setDirecao(Direcao.HORIZONTAL);
			grade.addPalavra(p);
			maior = Math.max(maior, e.palavra().length());
		}
		grade.setLinhas(entradas.isEmpty() ? 0 : entradas.size() * 2 - 1);
		grade.setColunas(maior);
	}

	private void removerConteudo(Atividade a) {
		Long id = a.getId();
		switch (a.getTipoAtividade()) {
		case QUIZ -> remover(Quiz.class, id);
		case JOGO_DA_MEMORIA -> remover(JogoMemoria.class, id);
		case CRUZADINHA -> remover(Cruzadinha.class, id);
		case LEITURA_GUIADA -> remover(LeituraGuiada.class, id);
		}
		em.flush();
	}

	private <T> void remover(Class<T> classe, Long id) {
		T anterior = em.find(classe, id);
		if (anterior != null)
			em.remove(anterior);
	}

	@Transactional(readOnly = true)
	public CriarAtividadeRequest buscarRascunho(Long id, String emailAutenticado) {
		Usuario usuario = usuarioService.getByEmail(emailAutenticado);
		if (!(usuario instanceof Professor professor)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo a professores.");
		}
		Atividade a = atividadeRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rascunho não encontrado."));
		if (a.getProfessor() == null || !professor.getId().equals(a.getProfessor().getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rascunho de outro professor.");
		}
		if (a.getStatus() != StatusAtividade.NAO_VINCULADO) {
			throw new IllegalArgumentException("A atividade já foi publicada e não pode ser editada nesta tela.");
		}
		String tipo = switch (a.getTipoAtividade()) {
		case QUIZ -> "quiz";
		case JOGO_DA_MEMORIA -> "memoria";
		case CRUZADINHA -> "cruzadas";
		case LEITURA_GUIADA -> "leitura";
		};
		List<PerguntaInput> perguntas = new ArrayList<>();
		List<ParInput> pares = new ArrayList<>();
		List<PalavraInput> palavras = new ArrayList<>();
		List<String> trechos = new ArrayList<>();
		switch (a.getTipoAtividade()) {
		case QUIZ -> {
			Quiz quiz = em.find(Quiz.class, id);
			if (quiz != null)
				for (Pergunta p : quiz.getPerguntas().stream().sorted(java.util.Comparator.comparing(Pergunta::getId))
						.toList()) {
					List<String> opcoes = List.of(texto(p.getAlternativa1()), texto(p.getAlternativa2()),
							texto(p.getAlternativa3()), texto(p.getAlternativa4()), texto(p.getAlternativa5()));
					List<String> visiveis = new ArrayList<>(opcoes);
					while (visiveis.size() > 2 && visiveis.get(visiveis.size() - 1).isBlank())
						visiveis.remove(visiveis.size() - 1);
					perguntas.add(new PerguntaInput(texto(p.getEnunciado()), visiveis, p.getAlternativaCorreta() - 1));
				}
		}
		case JOGO_DA_MEMORIA -> {
			JogoMemoria jogo = em.find(JogoMemoria.class, id);
			if (jogo != null)
				for (ParCarta par : jogo.getPares())
					pares.add(new ParInput(par.getCartaA(), par.getCartaB()));
		}
		case CRUZADINHA -> {
			Cruzadinha grade = em.find(Cruzadinha.class, id);
			if (grade != null)
				for (PalavraCruzada p : grade.getPalavraCruzada())
					palavras.add(new PalavraInput(p.getResposta(), p.getDica()));
		}
		case LEITURA_GUIADA -> {
			LeituraGuiada leitura = em.find(LeituraGuiada.class, id);
			if (leitura != null)
				leitura.getTrechos().stream().sorted(java.util.Comparator.comparingInt(TrechoLeitura::getOrdem))
						.forEach(t -> trechos.add(t.getTexto()));
		}
		}
		return new CriarAtividadeRequest(a.getNome(), a.getDescricao(), a.getPrazo(), tipo,
				new ArrayList<>(a.getDestinatariosRascunho()), perguntas, pares, palavras, trechos, "Rascunho");
	}

	private void salvarLeitura(Atividade a, List<String> trechos, String pdfExistente) {
		LeituraGuiada leitura = new LeituraGuiada();
		leitura.setAtividade(a);
		leitura.setArquivoPdf(pdfExistente);
		leitura.setPerguntasPorTrecho(2);
		leitura.setNivelEscolar("ENSINO_MEDIO");

		if (trechos != null) {
			for (String item : trechos) {
				if (!texto(item).isEmpty())
					leitura.addTrecho(new TrechoLeitura(leitura.getTrechos().size(), texto(item)));
			}
		}
		em.persist(leitura);
	}

	@Transactional
	public void anexarPdf(Long id, MultipartFile arquivo, String emailAutenticado) {
		Usuario usuario = usuarioService.getByEmail(emailAutenticado);
		if (!(usuario instanceof Professor professor))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas professores podem anexar PDFs.");
		Atividade a = atividadeRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rascunho não encontrado."));
		if (a.getProfessor() == null || !a.getProfessor().getId().equals(professor.getId()))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é autor desta atividade.");
		if (a.getStatus() != StatusAtividade.NAO_VINCULADO || a.getTipoAtividade() != TipoAtividade.LEITURA_GUIADA)
			throw new IllegalArgumentException("PDF só pode ser anexado a um rascunho de Leitura Guiada.");
		if (arquivo == null || arquivo.isEmpty() || arquivo.getSize() > 2 * 1024 * 1024)
			throw new IllegalArgumentException("Envie um PDF de até 2 MB.");
		Path destino = null;
		try (InputStream in = arquivo.getInputStream()) {
			byte[] cabecalho = in.readNBytes(5);
			if (cabecalho.length != 5 || !java.util.Arrays.equals(cabecalho,
					"%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII)))
				throw new IllegalArgumentException("O arquivo não possui cabeçalho PDF válido.");
			Path pasta = Path.of(pastaPdf).toAbsolutePath().normalize();
			Files.createDirectories(pasta);
			destino = pasta.resolve(UUID.randomUUID() + ".pdf").normalize();
			try (OutputStream saida = Files.newOutputStream(destino, StandardOpenOption.CREATE_NEW)) {
				saida.write(cabecalho);
				in.transferTo(saida);
			}
			LeituraGuiada leitura = em.find(LeituraGuiada.class, id);
			if (leitura == null)
				throw new IllegalArgumentException("Conteúdo de Leitura Guiada não encontrado.");
			leitura.setArquivoPdf(destino.toString());
			em.flush();
		} catch (IOException | RuntimeException e) {
			if (destino != null)
				try {
					Files.deleteIfExists(destino);
				} catch (IOException ignorado) {
				}
			if (e instanceof RuntimeException re)
				throw re;
			throw new IllegalStateException("Não foi possível guardar o arquivo PDF no servidor.", e);
		}
	}

	private static void validarQuiz(List<PerguntaInput> itens, boolean publicar) {
		if (itens == null || itens.size() > 50 || (publicar && itens.isEmpty())) {
			throw new IllegalArgumentException("Quiz precisa de 1 a 50 perguntas para envio.");
		}
		for (PerguntaInput item : itens) {
			if (item == null)
				throw new IllegalArgumentException("Pergunta inválida.");
			limitado(item.pergunta(), 255, "O enunciado", publicar);
			List<String> opcoes = item.opcoes() == null ? List.of() : item.opcoes();
			if (opcoes.size() > 5 || (publicar && opcoes.size() < 2)) {
				throw new IllegalArgumentException("Cada pergunta deve ter de 2 a 5 alternativas.");
			}
			for (String opcao : opcoes)
				limitado(opcao, 255, "A alternativa", publicar);
			if (publicar && (item.correta() == null || item.correta() < 0 || item.correta() >= opcoes.size())) {
				throw new IllegalArgumentException("Marque uma alternativa correta em cada pergunta.");
			}
		}
	}

	private static void validarMemoria(List<ParInput> itens, boolean publicar) {
		if (itens == null || itens.size() > 50 || (publicar && itens.size() < 2)) {
			throw new IllegalArgumentException("Memória precisa de pelo menos 2 pares para envio.");
		}
		for (ParInput par : itens) {
			if (par == null)
				throw new IllegalArgumentException("Par de cartas inválido.");
			limitado(par.termo(), 255, "Carta A", publicar);
			limitado(par.significado(), 255, "Carta B", publicar);
		}
	}

	private static void validarPalavras(List<PalavraInput> itens, boolean publicar) {
		if (itens == null || itens.size() > 25 || (publicar && itens.size() < 2)) {
			throw new IllegalArgumentException("Cruzadinha precisa de pelo menos 2 palavras para envio.");
		}
		Set<String> unicas = new HashSet<>();
		for (PalavraInput p : itens) {
			if (p == null)
				throw new IllegalArgumentException("Palavra inválida.");
			String palavra = normalizarPalavra(p.palavra());
			if (publicar && (!palavra.matches("[A-Z]{2,16}") || !unicas.add(palavra))) {
				throw new IllegalArgumentException("Use palavras únicas de 2 a 16 letras, sem espaços ou números.");
			}
			limitado(p.dica(), 255, "A dica", publicar);
		}
	}

	private static void validarLeitura(List<String> trechos) {
		if (trechos == null)
			return;
		if (trechos.size() > 100)
			throw new IllegalArgumentException("Limite de 100 trechos por leitura.");
		for (String trecho : trechos)
			limitado(trecho, 255, "O trecho de leitura", false);
	}

	private static String texto(String s) {
		return s == null ? "" : s.trim();
	}

	private static String limitado(String valor, int max, String campo, boolean obrigatorio) {
		String s = texto(valor);
		if (obrigatorio && s.isBlank())
			throw new IllegalArgumentException(campo + " é obrigatório(a).");
		if (s.length() > max)
			throw new IllegalArgumentException(
					campo + " excede " + max + " caracteres. É necessária ampliação da coluna no banco.");
		return s;
	}

	private static String normalizarPalavra(String s) {
		return Normalizer.normalize(texto(s), Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toUpperCase(Locale.ROOT);
	}
}
