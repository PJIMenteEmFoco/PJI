package br.edu.ifsp.mef.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import br.edu.ifsp.mef.model.CodigoVerificacao;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.CodigoVerificacaoRepository;
import br.edu.ifsp.mef.repository.UsuarioRepository;

@Service
public class EmailService {

	@Autowired
	UsuarioRepository usuarioRepository;
	@Autowired
	CodigoVerificacaoRepository codigoVerificacaoRepository;
	@Autowired
	UsuarioDetailsService usuarioService;
	@Autowired
	JavaMailSender mailSender;
	@Autowired
	SecureRandom random;

	@Async
	public void enviarConvite(String destinatario, String nomeTurma, String token) {
		String link = "localhost:8081/convite/" + token;

		SimpleMailMessage mensagem = new SimpleMailMessage();
		mensagem.setTo(destinatario);
		mensagem.setSubject("Convite para a turma " + nomeTurma + " - Mente em Foco");
		mensagem.setText("Olá!"

				+ "Você foi convidado(a) para participar da turma " + nomeTurma + " no Mente em Foco.\n"
				+ "Acesse o link abaixo para entrar: " + link

				+ "Se você não esperava este e-mail, pode ignorá-lo.");
		try {
			mailSender.send(mensagem);
		} catch (MailException e) {

		}

	}

	@Async
	public void enviarEmailRecuperacaoConta(String email, String assunto, String texto, String link, Long id) {

		SimpleMailMessage mensagem = new SimpleMailMessage();

		mensagem.setTo(email);
		mensagem.setSubject("Recuperação de conta - Mente em Foco");
		mensagem.setText("Ola! /n/n" + "Você desativou sua conta, caso tenha sido um acidente, "
				+ "clique no link abaixo para reativa-la" + link + usuarioService.getById(id));

		mailSender.send(mensagem);
	}

	@Async
	public void enviarCodigoRecuperacao(String email, String codigo, String nome) {

		SimpleMailMessage mensagem = new SimpleMailMessage();

		mensagem.setTo(email);
		mensagem.setSubject("Recuperação de senha - Mente em Foco");

		mensagem.setText("Olá! " + nome + "\n\n" + "Seu código para recuperação de senha é: " + codigo + "\n\n"
				+ "Se você não solicitou essa recuperação, ignore este e-mail.");

		mailSender.send(mensagem);
	}

	@Async
	public void envioNotificacaoAtividade(String email) {

		SimpleMailMessage mensagem = new SimpleMailMessage();

		mensagem.setTo(email);
		mensagem.setSubject("Nova atividade - Mente em Foco");

		mensagem.setText("Olá! \n\n" + "Há uma nova atividade disponivel para ser realizada!");
	}

	@Async
	public void envioNotificacaoIngressoTurma(String email, String linkConvite, String nomeAluno) {

		SimpleMailMessage mensagem = new SimpleMailMessage();

		mensagem.setTo(email);
		mensagem.setSubject("Convite de turma - Mente em Foco");

		mensagem.setText("Olá!" + nomeAluno + "\n\n" + "Você recebeu o convite de ingresso para uma nova turma! \n\n"
				+ "Link do convite:" + linkConvite);
	}

	public String gerarCodigo() {
		return String.format("%06d", random.nextInt(1000000));
	}

	public String salvarCodigoVerificacao(String email) {
		CodigoVerificacao codigo = new CodigoVerificacao();
		codigo.setCodigo(gerarCodigo());
		codigo.setExpiracao(LocalDateTime.now().plusMinutes(10));
		codigo.setUtilizado(false);
		codigo.setUsuario(usuarioService.getByEmail(email));
		return codigo.getCodigo();
	}

	public boolean verificarCodigo(String codigo, String email) {
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
		CodigoVerificacao verificacao = codigoVerificacaoRepository.findByUsuario(usuario).orElse(null);

		if (verificacao == null) {
			return false;
		}

		if (verificacao.isUtilizado()) {
			return false;
		}

		if (LocalDateTime.now().isAfter(verificacao.getExpiracao())) {
			return false;
		}

		if (!verificacao.getCodigo().equals(codigo)) {
			return false;
		}

		excluirCodigoVerificacao(verificacao);
		return true;
	}

	public void excluirCodigoVerificacao(CodigoVerificacao codigo) {
		codigoVerificacaoRepository.delete(codigo);
	}
}
