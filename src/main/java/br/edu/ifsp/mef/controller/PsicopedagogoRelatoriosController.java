package br.edu.ifsp.mef.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.Psicopedagogo;
import br.edu.ifsp.mef.model.TentativaAtividade;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.AlunoRepository;
import br.edu.ifsp.mef.model.RelatorioPsicopedagogo;
import br.edu.ifsp.mef.repository.RelatorioPsicopedagogoRepository;
import br.edu.ifsp.mef.repository.TentativaAtividadeRepository;
import br.edu.ifsp.mef.repository.TurmaRepository;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class PsicopedagogoRelatoriosController {

	@Autowired
	UsuarioDetailsService usuarioService;
	@Autowired
	TurmaRepository turmaRepository;
	@Autowired
	AlunoRepository alunoRepository;
	@Autowired
	TentativaAtividadeRepository tentativaRepository;
	@Autowired
	RelatorioPsicopedagogoRepository relatorioRepository;

	@GetMapping("/psicopedagogo/relatorios")
	@Transactional(readOnly = true)
	public String pagina(Authentication authentication, @RequestParam(name = "aluno", required = false) Long alunoId,
			Model model) {
		Psicopedagogo psicopedagogo = exigirPsicopedagogo(authentication);
		List<Aluno> alunos = alunosVinculados(psicopedagogo.getId());
		boolean autorizado = alunoId != null && alunos.stream().anyMatch(a -> a.getId().equals(alunoId));
		model.addAttribute("alunos", alunos);
		model.addAttribute("alunoSelecionadoId", autorizado ? alunoId : null);
		return "psicopedagogo/relatorios";
	}

	@GetMapping("/psicopedagogo/relatorios/escrever")
	@Transactional(readOnly = true)
	public String paginaEscrever(Authentication authentication,
			@RequestParam(name = "aluno", required = false) Long alunoId, Model model) {
		Psicopedagogo psicopedagogo = exigirPsicopedagogo(authentication);
		List<Aluno> alunos = alunosVinculados(psicopedagogo.getId());
		boolean autorizado = alunoId != null && alunos.stream().anyMatch(a -> a.getId().equals(alunoId));
		model.addAttribute("alunos", alunos);
		model.addAttribute("alunoSelecionadoId", autorizado ? alunoId : null);
		model.addAttribute("modoRelatorio", "criar");
		model.addAttribute("relatorioId", null);
		return "psicopedagogo/escrever-relatorio";
	}

	@GetMapping("/psicopedagogo/relatorios/manuais/{id}/editar")
	@Transactional(readOnly = true)
	public String paginaEditar(@PathVariable Long id, Authentication authentication, Model model) {
		Psicopedagogo psicopedagogo = exigirPsicopedagogo(authentication);
		RelatorioPsicopedagogo relatorio = relatorioRepository.findByIdAndPsicopedagogo_Id(id, psicopedagogo.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatório não encontrado."));
		alunoVinculado(psicopedagogo.getId(), relatorio.getAluno().getId());
		model.addAttribute("alunos", alunosVinculados(psicopedagogo.getId()));
		model.addAttribute("alunoSelecionadoId", relatorio.getAluno().getId());
		model.addAttribute("modoRelatorio", "editar");
		model.addAttribute("relatorioId", relatorio.getId());
		return "psicopedagogo/escrever-relatorio";
	}

	@GetMapping(value = "/psicopedagogo/relatorios/manuais/{id}/dados", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Transactional(readOnly = true)
	public RelatorioManualDto consultarRelatorioManual(@PathVariable Long id, Authentication authentication) {
		Psicopedagogo psicopedagogo = exigirPsicopedagogo(authentication);
		RelatorioPsicopedagogo relatorio = relatorioRepository.findByIdAndPsicopedagogo_Id(id, psicopedagogo.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatório não encontrado."));
		Aluno aluno = alunoVinculado(psicopedagogo.getId(), relatorio.getAluno().getId());
		return new RelatorioManualDto(relatorio.getId(), aluno.getId(), aluno.getNome(), relatorio.getTitulo(),
				relatorio.getObservacoes(), relatorio.getEstrategias(), relatorio.getProximosPassos());
	}

	@GetMapping(value = "/psicopedagogo/relatorios/dados", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Transactional(readOnly = true)
	public List<RelatorioDto> listar(Authentication authentication) {
		Psicopedagogo psicopedagogo = exigirPsicopedagogo(authentication);
		List<Aluno> alunos = alunosVinculados(psicopedagogo.getId());
		Collection<Long> alunoIds = alunos.stream().map(Aluno::getId).toList();
		if (alunoIds.isEmpty())
			return List.of();

		List<RelatorioDto> resultado = new ArrayList<>();
		for (TentativaAtividade tentativa : tentativaRepository
				.findByAluno_IdInOrderByFinalizadaEmDescIdDesc(alunoIds)) {
			Atividade atividade = tentativa.getAtividade();
			Aluno aluno = tentativa.getAluno();
			if (atividade == null || aluno == null)
				continue;
			int total = tentativa.getTotal();
			int acertos = tentativa.getAcertos();
			int erros = Math.max(0, total - acertos);
			int percentual = total <= 0 ? 0 : Math.round(acertos * 100.0f / total);
			String autor = atividade.getProfessor() == null ? "Sistema" : atividade.getProfessor().getNome();
			resultado.add(new RelatorioDto("automatico-" + tentativa.getId(), "automatico", tentativa.getId(),
					aluno.getId(), aluno.getNome(), atividade.getNome(), tentativa.getFinalizadaEm(), null, autor,
					atividade.getTipoAtividade() == null ? "" : atividade.getTipoAtividade().name(), acertos, total,
					erros, percentual, tentativa.getSegundos(), null, null, null, atividade.getId()));
		}

		for (RelatorioPsicopedagogo relatorio : relatorioRepository
				.findByPsicopedagogo_IdAndAluno_IdInOrderByDataCriacaoDesc(psicopedagogo.getId(), alunoIds)) {
			resultado.add(new RelatorioDto("manual-" + relatorio.getId(), "manual", relatorio.getId(),
					relatorio.getAluno().getId(), relatorio.getAluno().getNome(), relatorio.getTitulo(),
					relatorio.getDataCriacao(), relatorio.getDataAtualizacao(), psicopedagogo.getNome(), "", null, null,
					null, null, null, relatorio.getObservacoes(), relatorio.getEstrategias(),
					relatorio.getProximosPassos(), null));
		}

		resultado.sort(Comparator.comparing(RelatorioDto::finishedAt,
				Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())));
		return resultado;
	}

	@PostMapping(value = "/psicopedagogo/relatorios/manuais", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Transactional
	public Map<String, Object> criarRelatorioManual(@RequestBody RelatorioManualRequest request,
			Authentication authentication) {
		Psicopedagogo psicopedagogo = exigirPsicopedagogo(authentication);
		validar(request);
		Aluno aluno = alunoVinculado(psicopedagogo.getId(), request.alunoId());

		RelatorioPsicopedagogo relatorio = new RelatorioPsicopedagogo();
		relatorio.setPsicopedagogo(psicopedagogo);
		relatorio.setAluno(aluno);
		aplicarCampos(relatorio, request);
		RelatorioPsicopedagogo salvo = relatorioRepository.save(relatorio);
		return Map.of("sucesso", true, "id", salvo.getId(), "alunoId", salvo.getAluno().getId());
	}

	@PutMapping(value = "/psicopedagogo/relatorios/manuais/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Transactional
	public Map<String, Object> atualizarRelatorioManual(@PathVariable Long id,
			@RequestBody RelatorioManualRequest request, Authentication authentication) {
		Psicopedagogo psicopedagogo = exigirPsicopedagogo(authentication);
		validar(request);
		RelatorioPsicopedagogo relatorio = relatorioRepository.findByIdAndPsicopedagogo_Id(id, psicopedagogo.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatório não encontrado."));
		if (!relatorio.getAluno().getId().equals(request.alunoId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"O aluno de um relatório existente não pode ser alterado.");
		}
		alunoVinculado(psicopedagogo.getId(), request.alunoId());
		aplicarCampos(relatorio, request);
		relatorioRepository.save(relatorio);
		return Map.of("sucesso", true, "id", relatorio.getId(), "alunoId", relatorio.getAluno().getId());
	}

	private Psicopedagogo exigirPsicopedagogo(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para acessar os relatórios.");
		}
		Usuario usuario = usuarioService.getByEmail(authentication.getName());
		if (!(usuario instanceof Psicopedagogo psicopedagogo)
				|| !"PSICOPEDAGOGO".equalsIgnoreCase(usuario.getPerfil())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo aos psicopedagogos.");
		}
		return psicopedagogo;
	}

	private List<Aluno> alunosVinculados(Long psicopedagogoId) {
		return turmaRepository.buscarAlunosDoMembro(psicopedagogoId).stream().filter(Aluno.class::isInstance)
				.map(Aluno.class::cast).distinct()
				.sorted(Comparator.comparing(Aluno::getNome, String.CASE_INSENSITIVE_ORDER)).toList();
	}

	private Aluno alunoVinculado(Long psicopedagogoId, Long alunoId) {
		if (alunoId == null || alunosVinculados(psicopedagogoId).stream().noneMatch(a -> a.getId().equals(alunoId))) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Aluno não encontrado nas turmas deste psicopedagogo.");
		}
		return alunoRepository.findById(alunoId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado."));
	}

	private void validar(RelatorioManualRequest request) {
		if (request == null || request.alunoId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione um aluno.");
		}
		validarTexto(request.titulo(), "Informe o título do relatório.", 160);
		validarTexto(request.observacoes(), "Preencha as observações pedagógicas.", 20000);
		validarOpcional(request.estrategias(), "Estratégias utilizadas", 20000);
		validarOpcional(request.proximosPassos(), "Próximos passos", 20000);
	}

	private void validarTexto(String value, String mensagem, int max) {
		if (value == null || value.isBlank())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
		if (value.trim().length() > max) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um dos campos excede o limite permitido.");
		}
	}

	private void validarOpcional(String value, String campo, int max) {
		if (value != null && value.length() > max) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, campo + " excede o limite permitido.");
		}
	}

	private void aplicarCampos(RelatorioPsicopedagogo relatorio, RelatorioManualRequest request) {
		relatorio.setTitulo(request.titulo().trim());
		relatorio.setObservacoes(request.observacoes().trim());
		relatorio.setEstrategias(request.estrategias() == null ? "" : request.estrategias().trim());
		relatorio.setProximosPassos(request.proximosPassos() == null ? "" : request.proximosPassos().trim());
	}

	public record RelatorioManualRequest(Long alunoId, String titulo, String observacoes, String estrategias,
			String proximosPassos) {
	}

	public record RelatorioManualDto(Long id, Long alunoId, String alunoNome, String titulo, String observacoes,
			String estrategias, String proximosPassos) {
	}

	public record RelatorioDto(String id, String kind, Long sourceId, Long studentId, String studentName, String title,
			LocalDateTime finishedAt, LocalDateTime updatedAt, String author, String type, Integer correct,
			Integer total, Integer errors, Integer score, Integer seconds, String observations, String strategies,
			String nextSteps, Long taskId) {
	}
}
