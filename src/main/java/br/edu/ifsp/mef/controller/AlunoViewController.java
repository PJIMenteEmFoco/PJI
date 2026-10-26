package br.edu.ifsp.mef.controller;

import java.util.List;
import java.time.LocalDate;
import br.edu.ifsp.mef.model.StatusAtividade;
import br.edu.ifsp.mef.model.TentativaAtividade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.mef.service.AtividadeService;
import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.Calendario;
import br.edu.ifsp.mef.model.Evento;
import br.edu.ifsp.mef.model.Feriado;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.TentativaAtividadeRepository;
import br.edu.ifsp.mef.service.AlunoService;
import br.edu.ifsp.mef.service.CalendarioService;
import br.edu.ifsp.mef.service.FeriadoService;

@Controller
public class AlunoViewController {

	@Autowired
	AlunoService alunoService;
	@Autowired
	AtividadeService atividadeService;
	@Autowired
	CalendarioService calendarioService;
	@Autowired
	FeriadoService feriadoService;
	@Autowired
	TentativaAtividadeRepository tentativaAtividadeRepository;

	private void preencherAtividades(Model model, Long alunoId) {
		List<Atividade> todas = alunoService.buscarAtividades(alunoId);
		LocalDate hoje = LocalDate.now();
		model.addAttribute("atividadesPendentes", todas.stream().filter(a -> a.getStatus() != StatusAtividade.CONCLUIDO)
				.filter(a -> a.getPrazo() == null || !a.getPrazo().isBefore(hoje)).toList());
		model.addAttribute("atividadesAtrasadas", todas.stream().filter(a -> a.getStatus() != StatusAtividade.CONCLUIDO)
				.filter(a -> a.getPrazo() != null && a.getPrazo().isBefore(hoje)).toList());
		model.addAttribute("atividadesConcluidas",
				todas.stream().filter(a -> a.getStatus() == StatusAtividade.CONCLUIDO).toList());
	}

	@GetMapping("/aluno/dashboard")
	public String dashboard(Model model, Authentication authentication) {

		String emailLogado = authentication.getName();
		Usuario aluno = alunoService.getByEmail(emailLogado);
		preencherAtividades(model, aluno.getId());
		List<Atividade> atividades = alunoService.buscarAtividades(aluno.getId());
		long concluidas = atividades.stream().filter(a -> a.getStatus() == StatusAtividade.CONCLUIDO).count();
		model.addAttribute("metaConcluidas", concluidas);
		model.addAttribute("metaTotal", atividades.size());
		model.addAttribute("metaPorcentagem",
				atividades.isEmpty() ? 0 : Math.round(concluidas * 100.0 / atividades.size()));
		model.addAttribute("metaMensagem", "Seu progresso é atualizado conforme as atividades são concluídas.");

		return "aluno/dashboard_aluno";
	}

	@GetMapping("/aluno/tarefas")
	public String tarefas(Authentication authentication, Model model) {
		UsuarioDetails aluno = (UsuarioDetails) authentication.getPrincipal();
		preencherAtividades(model, aluno.getId());
		return "aluno/tarefas_aluno";
	}

	/*@GetMapping("/aluno/atividade/executar")
	public String executarAtividade(@RequestParam(required = false) Long id, Authentication authentication,
			Model model) {
		if (id != null) {
			Long usuarioId = ((UsuarioDetails) authentication.getPrincipal()).getId();
			alunoService.buscarAtividades(usuarioId).stream().filter(a -> id.equals(a.getId())).findFirst()
					.ifPresent(a -> model.addAttribute("atividade", a));
		}
		return "aluno/executar_atividade";
	}
*/
	@GetMapping("/aluno/desempenho")
	public String relatorioDesempenho(@RequestParam(name = "tentativaId", required = false) Long tentativaId,
			Authentication authentication, Model model) {
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
			return "redirect:/login";
		}
		if (!(authentication.getPrincipal() instanceof UsuarioDetails detalhes)
				|| !(detalhes.getUsuario() instanceof Aluno aluno)) {
			return "redirect:/aluno/tarefas";
		}
		if (tentativaId == null || tentativaId <= 0) {
			return "redirect:/aluno/tarefas";
		}

		TentativaAtividade tentativa = tentativaAtividadeRepository.buscarTentativaDoAluno(tentativaId, aluno.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Resultado não encontrado para este aluno."));

		int total = tentativa.getTotal();
		int percentual = total > 0 ? Math.round(tentativa.getAcertos() * 100.0f / total) : 0;
		int segundos = Math.max(0, tentativa.getSegundos());
		String tempoFormatado = String.format(java.util.Locale.ROOT, "%02d:%02d", segundos / 60, segundos % 60);
		String primeiroNome = detalhes.getPrimeiroNome();

		model.addAttribute("tituloResultado", "Atividade concluída, " + primeiroNome + "!");
		model.addAttribute("nomeAtividade", tentativa.getAtividade().getNome());
		model.addAttribute("pontuacao", percentual + "%");
		model.addAttribute("tempoGasto", tempoFormatado);
		model.addAttribute("xpGanho", tentativa.getXpRecebido() + " XP");
		model.addAttribute("atividadeId", tentativa.getAtividade().getId());
		return "aluno/relatorio_desempenho";
	}

	@GetMapping("/aluno/calendario")
	public String calendario(Authentication authentication, Model model) {
		UsuarioDetails usuarioLogado = (UsuarioDetails) authentication.getPrincipal();
		Aluno aluno = (Aluno) usuarioLogado.getUsuario();

		Calendario calendario = calendarioService.buscarOpcionalPorAluno(aluno)
				.orElseGet(() -> calendarioService.criarCalendario(aluno));
		List<Atividade> atividadesDoAluno = alunoService.buscarAtividades(usuarioLogado.getId());
		List<Evento> eventosCalendario = calendarioService.listarEventos(calendario.getId());

		int anoAtual = LocalDate.now().getYear();
		List<Feriado> feriados;
		try {
			feriados = feriadoService.buscarOuSincronizarAno(anoAtual);
		} catch (RuntimeException exception) {
			feriados = feriadoService.buscarEntre(LocalDate.of(anoAtual, 1, 1), LocalDate.of(anoAtual, 12, 31));
		}

		model.addAttribute("calendario", calendario);
		model.addAttribute("calendarioId", calendario.getId());
		model.addAttribute("eventosCalendario", eventosCalendario);
		model.addAttribute("atividadesDoAluno", atividadesDoAluno);
		model.addAttribute("feriados", feriados);
		return "aluno/calendario_aluno";
	}

	@GetMapping("/aluno/chat")
	public String chat(Authentication authentication, Model model) {
		UsuarioDetails aluno = (UsuarioDetails) authentication.getPrincipal();
		List<Usuario> contatos = alunoService.buscarContatosChat(aluno.getId());
		model.addAttribute("usuarioLogadoId", aluno.getId());
		model.addAttribute("contatos", contatos);
		model.addAttribute("usuarioLogado", aluno);

		return "aluno/chatAluno";
	}

	@GetMapping("/aluno/atividade/executar")
	public String executarAtividade(@RequestParam(required = false) Long id) {
		if (id == null || id <= 0) {
			return "redirect:/aluno/tarefas";
		}
		return "redirect:/aluno/tarefas/" + id + "/executar";
	}

	@GetMapping("/aluno/configuracoes")
	public String configuracoes() {
		return "aluno/configuracoes_aluno";
	}
}
