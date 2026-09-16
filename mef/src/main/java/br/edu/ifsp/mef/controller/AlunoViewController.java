package br.edu.ifsp.mef.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.UsuarioRepository;
import br.edu.ifsp.mef.service.AlunoService;

@Controller
public class AlunoViewController {

	@Autowired
	AlunoService alunoService;
    @Autowired
    UsuarioRepository usuarioRepository;
    
   
	
	@GetMapping("/aluno/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        
        String emailLogado = authentication.getName();
        Usuario aluno = usuarioRepository.findByEmailIgnoreCase(emailLogado).orElse(null);
        // 3. Passa as listas de tarefas (substitua pelos seus métodos reais do banco)
        // model.addAttribute("tarefasPendentes", tarefaService.buscarPendentes(aluno.getId()));
        // model.addAttribute("tarefasAtrasadas", tarefaService.buscarAtrasadas(aluno.getId()));

        // Meta da semana (pode deixar valores fixos por enquanto também, se preferir)
        model.addAttribute("metaConcluidas", 7);
        model.addAttribute("metaTotal", 10);
        model.addAttribute("metaPorcentagem", 70);
        model.addAttribute("metaMensagem", "Complete mais 3 atividades esta semana para ganhar a insígnia de Leitor Focado! 🎯");

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
	public String chat(Authentication authentication, Model model) {
		UsuarioDetails aluno = (UsuarioDetails) authentication.getPrincipal();
		List<Usuario> professores = alunoService.buscarAlunosTurma(aluno.getId());
	    model.addAttribute("usuarioLogadoId", aluno.getId());
	    model.addAttribute("professores", professores);
	    model.addAttribute("usuarioLogado", aluno);
		return "aluno/chatAluno";
	}


	@GetMapping("/aluno/configuracoes")
	public String configuracoes() {
		return "aluno/configuracoes_aluno";
	}
}
