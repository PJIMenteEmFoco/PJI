package br.edu.ifsp.mef.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import br.edu.ifsp.mef.model.ConviteTurma;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.ConviteService;
import br.edu.ifsp.mef.service.TurmaService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LinkAcessoController {

	@Autowired
	TurmaService turmaService;
	ConviteService conviteService;
	
	@GetMapping("/convite/{token}")
	public String acessarConvite(@PathVariable Long token, Usuario usuario, HttpServletRequest request) {
	    ConviteTurma convite = conviteService.validarToken(token);

	    if (usuario == null) {
	        request.getSession().setAttribute("tokenConvitePendente", token);
	        return "redirect:/login";
	    }
	    turmaService.adicionarUsuario(convite.getIdTurma(), usuario.getId());
	    conviteService.removerConvite(token);

	    return "redirect:/turmas/" + convite.getIdTurma() + "?sucesso=matricula_realizada";
	}
}
