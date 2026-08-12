package br.edu.ifsp.mef.controller;

import br.edu.ifsp.mef.repository.AtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class CadastroController {
	@Autowired
	UsuarioDetailsService usuarioDetailsService;
	@Autowired
	AtividadeRepository atividadeRepository;
	
	CadastroController(AtividadeRepository atividadeRepository) {
		this.atividadeRepository = atividadeRepository;
	}
	
	@GetMapping("/cadastro")
	public String cadastro(Model model) {
	    return "cadastro";
	}
	
	@PostMapping("/cadastrar")
	public String postCadastro(Usuario usuario, @RequestParam("perfil") String perfil) {
		usuarioDetailsService.cadastrarUsuario(usuario, perfil);
		return "login";
	}
}
