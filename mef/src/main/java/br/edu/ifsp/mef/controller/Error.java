package br.edu.ifsp.mef.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class Error implements ErrorController {

	@Autowired
	private UsuarioDetailsService usuarioService;

	@RequestMapping("/error")
	public String erro(HttpServletRequest request, Authentication authentication, Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if (authentication != null && authentication.isAuthenticated()) {

			Usuario usuario = usuarioService.getByEmail(authentication.getName());
			if (usuario != null) {
				model.addAttribute("perfilUsuario", usuario.getPerfil());
			}
		}
		Object caminhoErro = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

		model.addAttribute("caminhoErro", caminhoErro);
		/*
		if (status != null) {

			int statusCode = Integer.parseInt(status.toString());
			if (statusCode == 404) {
				model.addAttribute("codigoErro", 404);
				return "error/404";
			}
			if (statusCode == 403) {

				model.addAttribute("codigoErro", 403);
				return "error/403";
			}
			if (statusCode == 500) {

				model.addAttribute("codigoErro", 500);
				return "error/500";
			}
		}*/
		model.addAttribute("codigoErro", status);

		return "error/error";
	}
}