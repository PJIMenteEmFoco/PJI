package br.edu.ifsp.mef.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import br.edu.ifsp.mef.model.Turma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.UsuarioDetails;
import br.edu.ifsp.mef.repository.TurmaRepository;

@Controller
public class PsicopedagogoViewController {
    @Autowired private TurmaRepository turmaRepository;

    private Long usuarioId(Authentication auth) {
        return ((UsuarioDetails) auth.getPrincipal()).getId();
    }

    @GetMapping("/psicopedagogo/dashboard")
    public String dashboard(Authentication auth, Model model) {
        List<Turma> turmas = turmaRepository.buscarTurmasPorMembro(usuarioId(auth));
        model.addAttribute("turmas", turmas);
        List<Usuario> alunos = turmaRepository.buscarAlunosDoMembro(usuarioId(auth));
        model.addAttribute("alunos", alunos);
        model.addAttribute("quantidadeAlunos", alunos.size());
        return "psicopedagogo/dashboard_psico";
    }

    @GetMapping("/psicopedagogo/turmas")
    public String turmas(Authentication auth, Model model) {
        model.addAttribute("turmas", turmaRepository.buscarTurmasPorMembro(usuarioId(auth)));
        return "psicopedagogo/turmas_psico";
    }

    @GetMapping("/psicopedagogo/detalhes-turma-psico/{id}")
    public String detalhesTurma(@PathVariable Long id, Authentication auth, Model model) {
        Turma turma = turmaRepository.buscarTurmasPorMembro(usuarioId(auth)).stream()
           .filter(t -> t.getId().equals(id)).findFirst()
           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("turma", turma);
        model.addAttribute("alunos", turmaRepository.buscarAlunosDaTurma(id));
        return "psicopedagogo/detalhes-turma-psico";
    }

    @GetMapping("/psicopedagogo/tarefas")
    public String tarefas() { return "psicopedagogo/tarefas_psico"; }
    @GetMapping("/psicopedagogo/calendario")
    public String calendario() { return "psicopedagogo/calendario_psico"; }
    @GetMapping("/psicopedagogo/chat")
    public String chat(Authentication auth, Model model) {
        UsuarioDetails usuario = (UsuarioDetails) auth.getPrincipal();
        model.addAttribute("usuarioLogadoId", usuario.getId());
        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("alunos", turmaRepository.buscarAlunosDoMembro(usuario.getId()));
        return "psicopedagogo/chat_psico";
    }
    @GetMapping("/psicopedagogo/configuracoes")
    public String configuracoes() { return "psicopedagogo/configuracoes_psico"; }
}
