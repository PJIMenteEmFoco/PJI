package br.edu.ifsp.mef.controller;

import br.edu.ifsp.mef.service.ExecucaoAtividadeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.List;

@Controller
public class AlunoAtividadeExecucaoController {
    private final ExecucaoAtividadeService service;
    public AlunoAtividadeExecucaoController(ExecucaoAtividadeService service) { this.service = service; }
    private void exigirLogin(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal())))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para acessar a atividade.");
    }
    @GetMapping("/aluno/tarefas/{id}/executar")
    public String pagina(@PathVariable Long id, Authentication auth) {
        exigirLogin(auth);
        service.dados(id, auth.getName()); // Testa acesso antes de renderizar o template.
        return "aluno/executar_atividade";
    }
    @GetMapping(value = "/aluno/tarefas/dados", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String,Object>> minhasAtividades(Authentication auth) {
        exigirLogin(auth);
        return service.minhasAtividades(auth.getName());
    }

    @GetMapping(value = "/aluno/tarefas/{id}/dados", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> dados(@PathVariable Long id, Authentication auth) {
        exigirLogin(auth);
        return service.dados(id, auth.getName());
    }
    @PostMapping(value = "/aluno/tarefas/{id}/responder", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> responder(@PathVariable Long id,
            @RequestBody ExecucaoAtividadeService.Respostas dados, Authentication auth) {
        exigirLogin(auth);
        return service.concluir(id, auth.getName(), dados);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Map<String,Object>> erro(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("sucesso",false,"mensagem",e.getMessage()));
    }
}
