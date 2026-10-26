package br.edu.ifsp.mef.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.UsuarioDetailsService;

@Controller
public class PsicopedagogoConfiguracoesController {

	@Autowired
	UsuarioDetailsService usuarioService;

	@PostMapping(value = "/psicopedagogo/configuracoes/dados", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> atualizarDados(@RequestBody Map<String, String> dados,
			Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null
				|| "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
			return erro(HttpStatus.UNAUTHORIZED, "Faça login para alterar seus dados.");
		}

		Usuario usuario = usuarioService.getByEmail(authentication.getName());
		if (usuario == null) {
			return erro(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
		}
		if (!"PSICOPEDAGOGO".equalsIgnoreCase(String.valueOf(usuario.getPerfil()))) {
			return erro(HttpStatus.FORBIDDEN, "Acesso permitido somente a psicopedagogos.");
		}
		if (dados == null) {
			return erro(HttpStatus.BAD_REQUEST, "Informe o campo e o valor.");
		}

		String campo = dados.get("campo");
		String recebido = dados.get("valor");
		if (campo == null || recebido == null || recebido.isBlank()) {
			return erro(HttpStatus.BAD_REQUEST, "Campo e valor são obrigatórios.");
		}
		if (!"nome".equals(campo) && !"telefone".equals(campo)) {
			return erro(HttpStatus.BAD_REQUEST, "Campo não permitido.");
		}

		String valor = recebido.trim();
		boolean atualizado;
		if ("nome".equals(campo)) {
			if (valor.length() < 2 || valor.length() > 150) {
				return erro(HttpStatus.BAD_REQUEST, "O nome deve ter de 2 a 150 caracteres.");
			}
			atualizado = usuarioService.atualizarNome(usuario.getId(), valor);
		} else {
			if (!valor.matches("^\\+?[0-9()\\s-]{8,20}$")) {
				return erro(HttpStatus.BAD_REQUEST, "Telefone em formato inválido.");
			}
			long digitos = valor.chars().filter(Character::isDigit).count();
			if (digitos < 8 || digitos > 15) {
				return erro(HttpStatus.BAD_REQUEST, "Telefone deve conter de 8 a 15 dígitos.");
			}
			atualizado = usuarioService.atualizarTelefone(usuario.getId(), valor);
		}

		if (!atualizado) {
			return erro(HttpStatus.NOT_FOUND, "Não foi possível atualizar os dados do usuário.");
		}
		Map<String, Object> resposta = new LinkedHashMap<>();
		resposta.put("sucesso", true);
		resposta.put("campo", campo);
		resposta.put("valor", valor);
		resposta.put("mensagem", "Dados atualizados com sucesso.");
		return ResponseEntity.ok(resposta);
	}

	private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String mensagem) {
		Map<String, Object> resposta = new LinkedHashMap<>();
		resposta.put("sucesso", false);
		resposta.put("mensagem", mensagem);
		return ResponseEntity.status(status).body(resposta);
	}
}
