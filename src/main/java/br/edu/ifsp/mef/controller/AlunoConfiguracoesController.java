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
public class AlunoConfiguracoesController {

	@Autowired
	UsuarioDetailsService usuarioService;

	@PostMapping(value = "/aluno/configuracoes/dados", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> atualizarDados(@RequestBody Map<String, String> dados,
			Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null
				|| "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
			return erro(HttpStatus.UNAUTHORIZED, "Faça login para alterar seus dados.");
		}

		if (dados == null) {
			return erro(HttpStatus.BAD_REQUEST, "Informe o campo e o valor.");
		}

		String campo = dados.get("campo");
		String valorRecebido = dados.get("valor");
		if (campo == null || valorRecebido == null || valorRecebido.isBlank()) {
			return erro(HttpStatus.BAD_REQUEST, "Campo e valor são obrigatórios.");
		}

		if (!"nome".equals(campo) && !"telefone".equals(campo)) {
			return erro(HttpStatus.BAD_REQUEST, "Campo não permitido.");
		}

		String valor = valorRecebido.trim();
		Usuario usuario = usuarioService.getByEmail(authentication.getName());
		if (usuario == null) {
			return erro(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
		}

		if (!"ALUNO".equalsIgnoreCase(String.valueOf(usuario.getPerfil()))) {
			return erro(HttpStatus.FORBIDDEN, "Acesso permitido somente a alunos.");
		}

		if ("nome".equals(campo)) {
			if (valor.length() < 2 || valor.length() > 150) {
				return erro(HttpStatus.BAD_REQUEST, "O nome deve ter de 2 a 150 caracteres.");
			}
			if (!usuarioService.atualizarNome(usuario.getId(), valor)) {
				return erro(HttpStatus.NOT_FOUND, "Não foi possível atualizar o nome.");
			}
		} else {
			if (!valor.matches("^\\+?[0-9()\\s-]{8,20}$")) {
				return erro(HttpStatus.BAD_REQUEST, "Telefone em formato inválido.");
			}
			long quantidadeDigitos = valor.chars().filter(Character::isDigit).count();
			if (quantidadeDigitos < 8 || quantidadeDigitos > 15) {
				return erro(HttpStatus.BAD_REQUEST, "Telefone deve conter de 8 a 15 dígitos.");
			}
			if (!usuarioService.atualizarTelefone(usuario.getId(), valor)) {
				return erro(HttpStatus.NOT_FOUND, "Não foi possível atualizar o telefone.");
			}
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
