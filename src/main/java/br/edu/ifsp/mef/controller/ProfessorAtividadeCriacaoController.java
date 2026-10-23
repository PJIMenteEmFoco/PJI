package br.edu.ifsp.mef.controller;

import br.edu.ifsp.mef.model.CriarAtividadeRequest;
import br.edu.ifsp.mef.service.CriarAtividadeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.model.Professor;
import br.edu.ifsp.mef.service.ProfessorService;
import br.edu.ifsp.mef.service.UsuarioDetailsService;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/** API isolada: não altera as rotas GET do ProfessorViewController. */
@Controller
public class ProfessorAtividadeCriacaoController {

    private final CriarAtividadeService criacao;
    private final ProfessorService professorService;
    private final UsuarioDetailsService usuarioService;

    public ProfessorAtividadeCriacaoController(CriarAtividadeService criacao,
            ProfessorService professorService, UsuarioDetailsService usuarioService) {
        this.criacao = criacao;
        this.professorService = professorService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/professor/tarefas/{id}/editar")
    public String telaEdicao(@PathVariable Long id, Authentication auth, Model model) {
        exigirLogin(auth);
        Usuario usuario = usuarioService.getByEmail(auth.getName());
        if (!(usuario instanceof Professor professor))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo a professores.");
        criacao.buscarRascunho(id, auth.getName()); // Validação antecipada de propriedade e status.
        List<Usuario> alunos = professorService.buscarAlunosTurma(professor.getId());
        model.addAttribute("alunos", alunos);
        model.addAttribute("salvarAtividadeUrl", "/professor/tarefas/" + id);
        model.addAttribute("carregarRascunhoUrl", "/professor/tarefas/" + id + "/dados");
        return "professor/criar-nova-tarefa";
    }

    @GetMapping(value = "/professor/tarefas/{id}/dados", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CriarAtividadeRequest dados(@PathVariable Long id, Authentication auth) {
        exigirLogin(auth);
        return criacao.buscarRascunho(id, auth.getName());
    }

    @PutMapping(value = "/professor/tarefas/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> editar(@PathVariable Long id, @RequestBody CriarAtividadeRequest dados, Authentication auth) {
        exigirLogin(auth);
        CriarAtividadeService.Resultado result = criacao.atualizarRascunho(id, dados, auth.getName());
        return Map.of("sucesso", true, "id", result.id(), "status", result.status(), "mensagem", result.mensagem());
    }

    private static void exigirLogin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para salvar atividades.");
        }
    }

    @PostMapping(value = "/professor/tarefas/salvar",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salvar(
            @RequestBody CriarAtividadeRequest dados,
            Authentication authentication) {

        exigirLogin(authentication);
        CriarAtividadeService.Resultado resultado = criacao.salvar(dados, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "sucesso", true,
                "id", resultado.id(),
                "status", resultado.status(),
                "mensagem", resultado.mensagem()));
    }

    @PostMapping(value = "/professor/tarefas/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> anexarPdf(@PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo, Authentication auth) {
        exigirLogin(auth);
        criacao.anexarPdf(id, arquivo, auth.getName());
        return Map.of("sucesso", true, "mensagem", "PDF anexado ao rascunho para processamento futuro.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> entradaInvalida(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("sucesso", false, "mensagem", e.getMessage()));
    }
}
