package br.edu.ifsp.mef.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class ConfiguracoesController {

	@Autowired
	UsuarioDetailsService usuarioDetailsService;
	
	@GetMapping("/configuracoes")
	public String getConfiguracoes() {
		return "/configuracoes";
	}
	
	@PostMapping("/desativarConta/{id}")
	public String postDesativarConta(@PathVariable Long id) {
		usuarioDetailsService.alterarStatusUsuario(id);
		return "redirect:/login";
	}
	
	 @PostMapping("/atualizarTelefone/{id}")
	    public String atualizarTelefone(
	            @PathVariable Long id,
	            @RequestParam String telefone) {

	        usuarioDetailsService.atualizarTelefone(id, telefone);
	        
	        return "redirect:/configuracoes";
	    }
	
	 @PostMapping("/atualizarNome/{id}")
	    public String atualizarNome(
	            @PathVariable Long id,
	            @RequestParam String nome) {

	        usuarioDetailsService.atualizarNome(id, nome);

	        return "redirect:/configuracoes";
	    }	
	
	 @PostMapping("/atualizarSenha/{id}")
	    public String atualizarSenha(
	            @PathVariable Long id,
	            @RequestParam String senha) {

	        usuarioDetailsService.atualizarSenha(id, senha);
	        
	        return "redirect:/configuracoes";
	    }
}
