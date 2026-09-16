package br.edu.ifsp.mef.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serve as telas do perfil ALUNO.
 * Por enquanto os metodos so retornam a view (sem dados dinamicos) --
 * a ligacao com dados reais (turmas, atividades, etc.) ainda precisa ser feita.
 */
@Controller
public class AlunoViewController {

	@GetMapping("/aluno/dashboard")
	public String dashboard() {
		return "aluno/dashboard_aluno";
	}

	@GetMapping("/aluno/tarefas")
	public String tarefas() {
		return "aluno/tarefas_aluno";
	}

	@GetMapping("/aluno/atividade/executar")
	public String executarAtividade() {
		return "aluno/executar_atividade";
	}

	@GetMapping("/aluno/desempenho")
	public String relatorioDesempenho() {
		return "aluno/relatorio_desempenho";
	}

	@GetMapping("/aluno/chat")
	public String chat() {
		return "aluno/chatAluno";
	}

	@GetMapping("/aluno/configuracoes")
	public String configuracoes() {
		return "aluno/configuracoes_aluno";
	}
}
