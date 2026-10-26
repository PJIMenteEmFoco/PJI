package br.edu.ifsp.mef.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.mef.model.CodigoVerificacao;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.CodigoVerificacaoRepository;
import br.edu.ifsp.mef.repository.UsuarioRepository;

@Service
public class RecuperacaoSenhaService {

	@Autowired
	UsuarioRepository usuarioRepository;
	@Autowired
	CodigoVerificacaoRepository codigoRepository;
	@Autowired
	EmailService emailService;
	@Autowired
	UsuarioDetailsService usuarioDetailsService;

	@Transactional
	public boolean solicitarCodigo(String email) {
		if (email == null || email.isBlank())
			return false;

		Optional<Usuario> usuarioOptional = usuarioRepository.findByEmailIgnoreCase(email.trim());
		if (usuarioOptional.isEmpty())
			return false;

		Usuario usuario = usuarioOptional.get();
		CodigoVerificacao codigo = codigoRepository.findByUsuario(usuario).orElseGet(CodigoVerificacao::new);
		codigo.setUsuario(usuario);
		codigo.setCodigo(emailService.gerarCodigo());
		codigo.setExpiracao(LocalDateTime.now().plusMinutes(10));
		codigo.setUtilizado(false);
		codigoRepository.save(codigo);

		emailService.enviarCodigoRecuperacao(usuario.getEmail(), codigo.getCodigo(), usuario.getNome());
		return true;
	}

	@Transactional
	public boolean verificarCodigo(String email, String codigoInformado) {
		if (email == null || email.isBlank() || codigoInformado == null || !codigoInformado.matches("\\d{6}")) {
			return false;
		}

		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
		if (usuario == null)
			return false;

		CodigoVerificacao codigo = codigoRepository.findByUsuario(usuario).orElse(null);
		if (codigo == null || codigo.isUtilizado() || codigo.getExpiracao() == null
				|| LocalDateTime.now().isAfter(codigo.getExpiracao()) || !codigoInformado.equals(codigo.getCodigo())) {
			return false;
		}

		codigoRepository.delete(codigo);
		return true;
	}

	public Optional<Usuario> buscarUsuario(String email) {
		if (email == null || email.isBlank())
			return Optional.empty();
		return usuarioRepository.findByEmailIgnoreCase(email.trim());
	}

	public boolean senhaAtendePolitica(String senha) {
		if (senha == null || senha.length() < 8)
			return false;
		boolean maiuscula = senha.chars().anyMatch(Character::isUpperCase);
		boolean minuscula = senha.chars().anyMatch(Character::isLowerCase);
		boolean numero = senha.chars().anyMatch(Character::isDigit);
		boolean especial = senha.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
		return maiuscula && minuscula && numero && especial;
	}

	public boolean alterarSenha(Long usuarioId, String novaSenha) {
		if (usuarioId == null || !senhaAtendePolitica(novaSenha))
			return false;
		return usuarioDetailsService.atualizarSenha(usuarioId, novaSenha);
	}
}
