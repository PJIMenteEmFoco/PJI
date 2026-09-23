package br.edu.ifsp.mef.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.Calendario;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.UsuarioRepository;
import br.edu.ifsp.mef.service.AlunoService;
import br.edu.ifsp.mef.service.CalendarioService;

@Controller
public class AlunoViewController {

	@Autowired
	AlunoService alunoService;
	@Autowired
	CalendarioService calendarioService;
    @Autowired
    UsuarioRepository usuarioRepository;
	
	@GetMapping("/aluno/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        
        String emailLogado = authentication.getName();
        Usuario aluno = usuarioRepository.findByEmailIgnoreCase(emailLogado).orElse(null);
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
	
	@GetMapping("/aluno/calendario")
	public String calendario(Authentication authentication, Model model) {
		UsuarioDetails aluno = (UsuarioDetails) authentication.getPrincipal();
	    Calendario calendario = calendarioService.buscarPorAluno((Aluno) aluno.getUsuario());
	    List<Atividade> atividadesDoAluno = alunoService.buscarAtividades(aluno.getId());
	    model.addAttribute("calendario", calendario);
	    model.addAttribute("atividadesDoAluno", atividadesDoAluno);
		return "aluno/calendario_aluno";
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
