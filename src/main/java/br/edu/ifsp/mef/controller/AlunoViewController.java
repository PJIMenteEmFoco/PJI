	package br.edu.ifsp.mef.controller;

import java.util.List;
import java.time.LocalDate;
import br.edu.ifsp.mef.model.StatusAtividade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import br.edu.ifsp.mef.service.AtividadeService;
import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.Calendario;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.service.AlunoService;
import br.edu.ifsp.mef.service.CalendarioService;

@Controller
public class AlunoViewController {

	@Autowired
	AlunoService alunoService;
    @Autowired
    AtividadeService atividadeService;
	@Autowired
	CalendarioService calendarioService;

    private void preencherAtividades(Model model, Long alunoId) {
        List<Atividade> todas = alunoService.buscarAtividades(alunoId);
        LocalDate hoje = LocalDate.now();
        model.addAttribute("atividadesPendentes", todas.stream()
           .filter(a -> a.getStatus() != StatusAtividade.CONCLUIDO)
           .filter(a -> a.getPrazo() == null || !a.getPrazo().isBefore(hoje)).toList());
        model.addAttribute("atividadesAtrasadas", todas.stream()
           .filter(a -> a.getStatus() != StatusAtividade.CONCLUIDO)
           .filter(a -> a.getPrazo() != null && a.getPrazo().isBefore(hoje)).toList());
        model.addAttribute("atividadesConcluidas", todas.stream()
           .filter(a -> a.getStatus() == StatusAtividade.CONCLUIDO).toList());
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
        model.addAttribute("metaPorcentagem", atividades.isEmpty() ? 0 : Math.round(concluidas * 100.0 / atividades.size()));
        model.addAttribute("metaMensagem", "Seu progresso é atualizado conforme as atividades são concluídas.");

        return "aluno/dashboard_aluno";
    }
 
	@GetMapping("/aluno/tarefas")
	public String tarefas(Authentication authentication, Model model) {
        UsuarioDetails aluno = (UsuarioDetails) authentication.getPrincipal();
        preencherAtividades(model, aluno.getId());
		return "aluno/tarefas_aluno";
	}

	@GetMapping("/aluno/atividade/executar")
	public String executarAtividade(@RequestParam(required = false) Long id, Authentication authentication, Model model) {
        if (id != null) {
            // Only show activities actually assigned to this authenticated student.
            Long usuarioId = ((UsuarioDetails) authentication.getPrincipal()).getId();
            alunoService.buscarAtividades(usuarioId).stream().filter(a -> id.equals(a.getId())).findFirst()
                .ifPresent(a -> model.addAttribute("atividade", a));
        }
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
