package br.edu.ifsp.mef.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AlunoFazerTarefaController {

    @GetMapping({"/aluno/fazer_tarefa", "/aluno/fazer-tarefa"})
    public String abrir(@RequestParam(required = false) Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            return "redirect:/login";
        }
        if (id == null || id <= 0) {
            return "aluno/fazer_tarefa";
        }
        return "redirect:/aluno/tarefas/" + id + "/executar";
    }
}
