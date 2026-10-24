package br.edu.ifsp.mef.controller;

import br.edu.ifsp.mef.model.CriarAtividadeRequest;
import br.edu.ifsp.mef.model.Psicopedagogo;
import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.CriarAtividadeService;
import br.edu.ifsp.mef.service.UsuarioDetailsService;
import br.edu.ifsp.mef.repository.TurmaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class PsicopedagogoAtividadeCriacaoController {

	private final CriarAtividadeService criacao;
	private final UsuarioDetailsService usuarioService;
	private final TurmaRepository turmaRepository;

	public PsicopedagogoAtividadeCriacaoController(CriarAtividadeService criacao, UsuarioDetailsService usuarioService,
			TurmaRepository turmaRepository) {
		this.criacao = criacao;
		this.usuarioService = usuarioService;
		this.turmaRepository = turmaRepository;
	}

	@GetMapping("/psicopedagogo/tarefas/nova")
	public String nova(@RequestParam(required = false) String tipo, Authentication auth, Model model) {
		Psicopedagogo psicopedagogo = exigirPsicopedagogo(auth);
		carregarModelo(psicopedagogo, model);
		model.addAttribute("salvarAtividadeUrl", "/psicopedagogo/tarefas/salvar");
		model.addAttribute("carregarRascunhoUrl", "");
		model.addAttribute("tipoInicial", tipo == null ? "quiz" : tipo);
		return "psicopedagogo/criar-nova-tarefa";
	}

	@GetMapping("/psicopedagogo/tarefas/{id}/editar")
	public String editarTela(@PathVariable Long id, Authentication auth, Model model) {
		Psicopedagogo psicopedagogo = exigirPsicopedagogo(auth);
		criacao.buscarRascunho(id, auth.getName());
		carregarModelo(psicopedagogo, model);
		model.addAttribute("salvarAtividadeUrl", "/psicopedagogo/tarefas/" + id);
		model.addAttribute("carregarRascunhoUrl", "/psicopedagogo/tarefas/" + id + "/dados");
		model.addAttribute("tipoInicial", "quiz");
		return "psicopedagogo/criar-nova-tarefa";
	}

	@GetMapping(value = "/psicopedagogo/tarefas/{id}/dados", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public CriarAtividadeRequest dados(@PathVariable Long id, Authentication auth) {
		exigirPsicopedagogo(auth);
		return criacao.buscarRascunho(id, auth.getName());
	}

	@PostMapping(value = "/psicopedagogo/tarefas/salvar", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> salvar(@RequestBody CriarAtividadeRequest dados, Authentication auth) {
		exigirPsicopedagogo(auth);
		CriarAtividadeService.Resultado resultado = criacao.salvar(dados, auth.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("sucesso", true, "id", resultado.id(), "status",
				resultado.status(), "mensagem", resultado.mensagem()));
	}

	@PutMapping(value = "/psicopedagogo/tarefas/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> atualizar(@PathVariable Long id, @RequestBody CriarAtividadeRequest dados,
			Authentication auth) {
		exigirPsicopedagogo(auth);
		CriarAtividadeService.Resultado resultado = criacao.atualizarRascunho(id, dados, auth.getName());
		return Map.of("sucesso", true, "id", resultado.id(), "status", resultado.status(), "mensagem",
				resultado.mensagem());
	}

	@PostMapping(value = "/psicopedagogo/tarefas/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> anexarPdf(@PathVariable Long id, @RequestParam("arquivo") MultipartFile arquivo,
			Authentication auth) {
		exigirPsicopedagogo(auth);
		criacao.anexarPdf(id, arquivo, auth.getName());
		return Map.of("sucesso", true, "mensagem", "PDF anexado ao rascunho para processamento futuro.");
	}

	private Psicopedagogo exigirPsicopedagogo(Authentication auth) {
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para continuar.");
		}
		Usuario usuario = usuarioService.getByEmail(auth.getName());
		if (!(usuario instanceof Psicopedagogo psicopedagogo)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo a psicopedagogos.");
		}
		return psicopedagogo;
	}

	private void carregarModelo(Psicopedagogo psicopedagogo, Model model) {
		List<Usuario> alunos = turmaRepository.buscarAlunosDoMembro(psicopedagogo.getId()).stream()
				.sorted(Comparator.comparing(Usuario::getNome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.toList();
		Map<Long, Set<String>> nomesTurmas = new LinkedHashMap<>();
		for (Turma turma : turmaRepository.buscarTurmasPorMembro(psicopedagogo.getId())) {
			String nomeTurma = turma.getNome() == null || turma.getNome().isBlank() ? "Turma " + turma.getId()
					: turma.getNome();
			for (Usuario membro : turma.getUsuarios()) {
				if ("ALUNO".equalsIgnoreCase(membro.getPerfil())) {
					nomesTurmas.computeIfAbsent(membro.getId(), ignored -> new LinkedHashSet<>()).add(nomeTurma);
				}
			}
		}
		Map<Long, String> turmasPorAluno = new LinkedHashMap<>();
		nomesTurmas.forEach((id, nomes) -> turmasPorAluno.put(id, String.join(", ", nomes)));
		model.addAttribute("alunos", alunos);
		model.addAttribute("turmasPorAluno", turmasPorAluno);
		model.addAttribute("salvarAtividadeUrl", "/psicopedagogo/tarefas/salvar");
		model.addAttribute("carregarRascunhoUrl", "");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> entradaInvalida(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body(Map.of("sucesso", false, "mensagem",
				e.getMessage() == null ? "Os dados informados são inválidos." : e.getMessage()));
	}
}
