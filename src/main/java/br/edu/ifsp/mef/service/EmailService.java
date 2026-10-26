package br.edu.ifsp.mef.service;

import java.security.SecureRandom;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	@Autowired
	JavaMailSender mailSender;
	@Autowired
	SecureRandom random;
	@Value("${app.base-url:http://localhost:8081}")
	String baseUrl;

	public void enviarConvite(String destinatario, String nomeTurma, String token) {
		URI base = URI.create(baseUrl);
		if (!("http".equals(base.getScheme()) || "https".equals(base.getScheme())) || base.getHost() == null
				|| base.getQuery() != null || base.getFragment() != null || base.getUserInfo() != null)
			throw new IllegalArgumentException("Configure app.base-url com a URL pública do site.");
		String link = baseUrl.replaceAll("/+$", "") + "/convite/" + java.util.UUID.fromString(token);
		SimpleMailMessage mensagem = new SimpleMailMessage();
		mensagem.setTo(destinatario);
		mensagem.setSubject("Convite para a turma " + nomeTurma + " - Mente em Foco");
		mensagem.setText("Olá!\n\n Você foi convidado(a) para a turma " + nomeTurma
				+ ". Entre com este e-mail para aceitar o convite, válido por 7 dias:\n" + link
				+ "\n\nSe não esperava este convite, ignore a mensagem.");
		mailSender.send(mensagem);
	}

	public void enviarCodigoRecuperacao(String email, String codigo, String nome) {

		SimpleMailMessage mensagem = new SimpleMailMessage();

		mensagem.setTo(email);
		mensagem.setSubject("Recuperação de senha - Mente em Foco");

		mensagem.setText("Olá! " + nome + "\n\n" + "Seu código para recuperação de senha é: " + codigo + "\n\n"
				+ "Se você não solicitou essa recuperação, ignore este e-mail.");

		mailSender.send(mensagem);
	}

	public String gerarCodigo() {
		return String.format("%06d", random.nextInt(1000000));
	}
}
