package br.edu.ifsp.mef.controller;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import br.edu.ifsp.mef.service.ConviteService;
import jakarta.servlet.http.HttpServletResponse;
@Controller
public class LinkAcessoController {
    private final ConviteService convites;
    public LinkAcessoController(ConviteService convites) { this.convites = convites; }
    @GetMapping("/convite/{token}")
    public String visualizar(@PathVariable String token, Authentication authentication, Model model) {
        model.addAttribute("turma", convites.consultar(token, authentication.getName()));
        model.addAttribute("token", token);
        return "convite";
    }
    @PostMapping("/convite/{token}")
    public String aceitar(@PathVariable String token, Authentication authentication) {
        return "redirect:" + convites.aceitar(token, authentication.getName());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public String erro(IllegalArgumentException erro, Model model, HttpServletResponse response) {
        response.setStatus(400); model.addAttribute("erro", erro.getMessage()); return "convite";
    }
}
