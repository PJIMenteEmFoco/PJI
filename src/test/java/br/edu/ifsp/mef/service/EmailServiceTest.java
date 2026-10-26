package br.edu.ifsp.mef.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
class EmailServiceTest {
    @Test void conviteIncluiUrlPublicaCompleta() {
        EmailService service = new EmailService(); service.mailSender = mock(JavaMailSender.class);
        service.baseUrl = "https://mef.example.com/";
        String token = UUID.randomUUID().toString();
        service.enviarConvite("aluno@example.com", "Português", token);
        var captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(service.mailSender).send(captor.capture());
        assertTrue(captor.getValue().getText().contains("https://mef.example.com/convite/" + token));
        assertEquals("aluno@example.com", captor.getValue().getTo()[0]);
    }
    @Test void falhaDeEnvioNaoEhIgnorada() {
        EmailService service = new EmailService(); service.mailSender = mock(JavaMailSender.class);
        service.baseUrl = "http://localhost:8081";
        doThrow(new MailSendException("SMTP indisponível")).when(service.mailSender).send(any(SimpleMailMessage.class));
        assertThrows(MailSendException.class, () -> service.enviarConvite("aluno@example.com", "Turma", UUID.randomUUID().toString()));
    }
}
