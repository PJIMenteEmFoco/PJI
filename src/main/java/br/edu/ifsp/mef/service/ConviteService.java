package br.edu.ifsp.mef.service;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.ifsp.mef.model.*;
import br.edu.ifsp.mef.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
@Service
public class ConviteService {
    private final ConviteTurmaRepository convites;
    private final UsuarioRepository usuarios;
    private final EntityManager entityManager;
    public ConviteService(ConviteTurmaRepository convites, UsuarioRepository usuarios, EntityManager entityManager) {
        this.convites = convites; this.usuarios = usuarios; this.entityManager = entityManager;
    }
    public static String normalizarEmail(String email) {
        String valor = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (valor.length() > 254 || !valor.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("Informe um e-mail válido.");
        return valor;
    }
    @Transactional
    public ConviteTurma criarConvite(Long turmaId, String email) {
        ConviteTurma convite = new ConviteTurma();
        convite.setIdTurma(turmaId); convite.setEmail(normalizarEmail(email));
        convite.setToken(UUID.randomUUID().toString());
        convite.setExpiracao(LocalDateTime.now().plusDays(7));
        return convites.save(convite);
    }
    private void validarFormato(String token) {
        if (token == null || !token.matches("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}"))
            throw new IllegalArgumentException("Convite inválido. Solicite um novo convite.");
    }
    private Usuario validarDestinatario(ConviteTurma convite, String email) {
        if (convite.getExpiracao() == null || !convite.getExpiracao().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Este convite expirou. Solicite um novo convite.");
        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));
        if (!usuario.isAtivado() || convite.getEmail() == null || !convite.getEmail().equalsIgnoreCase(usuario.getEmail()))
            throw new IllegalArgumentException("Entre com a conta do e-mail que recebeu este convite.");
        if (!"ALUNO".equals(usuario.getPerfil()) && !"PSICOPEDAGOGO".equals(usuario.getPerfil()))
            throw new IllegalArgumentException("Este convite é destinado a alunos ou psicopedagogos.");
        return usuario;
    }
    @Transactional(readOnly = true)
    public Turma consultar(String token, String email) {
        validarFormato(token);
        ConviteTurma convite = convites.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Convite inválido ou já utilizado."));
        validarDestinatario(convite, email);
        Turma turma = entityManager.find(Turma.class, convite.getIdTurma());
        if (turma == null) throw new IllegalArgumentException("Esta turma não existe mais.");
        return turma;
    }
    @Transactional
    public String aceitar(String token, String email) {
        validarFormato(token);
        ConviteTurma convite = convites.buscarParaAceitar(token)
            .orElseThrow(() -> new IllegalArgumentException("Convite inválido ou já utilizado."));
        Usuario usuario = validarDestinatario(convite, email);
        Turma turma = entityManager.find(Turma.class, convite.getIdTurma(), LockModeType.PESSIMISTIC_WRITE);
        if (turma == null) throw new IllegalArgumentException("Esta turma não existe mais.");
        if (turma.getUsuarios().stream().noneMatch(u -> u.getId().equals(usuario.getId()))) turma.addUsuario(usuario);
        convites.delete(convite);
        return "ALUNO".equals(usuario.getPerfil()) ? "/aluno/dashboard" : "/psicopedagogo/turmas";
    }
}
