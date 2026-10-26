package br.edu.ifsp.mef.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import br.edu.ifsp.mef.model.*;
import br.edu.ifsp.mef.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
class ConviteServiceTest {
    ConviteTurmaRepository convites;
    UsuarioRepository usuarios;
    EntityManager em;
    ConviteService service;
    ConviteTurma convite;
    Usuario aluno;
    Turma turma;
    String token;
    @BeforeEach void preparar() {
        convites = mock(ConviteTurmaRepository.class); usuarios = mock(UsuarioRepository.class);
        em = mock(EntityManager.class); service = new ConviteService(convites, usuarios, em);
        token = UUID.randomUUID().toString();
        convite = new ConviteTurma(); convite.setId(1L); convite.setIdTurma(2L);
        convite.setEmail("aluno@example.com"); convite.setToken(token); convite.setExpiracao(LocalDateTime.now().plusDays(1));
        aluno = new Aluno(); aluno.setId(3L); aluno.setPerfil("ALUNO"); aluno.setEmail("aluno@example.com"); aluno.setAtivado(true);
        turma = new Turma(); turma.setId(2L);
        when(convites.findByToken(token)).thenReturn(Optional.of(convite));
        when(convites.buscarParaAceitar(token)).thenReturn(Optional.of(convite));
        when(usuarios.findByEmailIgnoreCase(aluno.getEmail())).thenReturn(Optional.of(aluno));
        when(em.find(Turma.class, 2L)).thenReturn(turma);
        when(em.find(Turma.class, 2L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(turma);
    }
    @Test void consultarNaoMatriculaNemConsome() {
        assertSame(turma, service.consultar(token, aluno.getEmail()));
        assertTrue(turma.getUsuarios().isEmpty()); verify(convites, never()).delete(any());
    }
    @Test void aceitarMatriculaEConsome() {
        assertEquals("/aluno/dashboard", service.aceitar(token, aluno.getEmail()));
        assertTrue(turma.getUsuarios().contains(aluno)); verify(convites).delete(convite);
    }
    @Test void outroEmailNaoPodeAceitar() {
        convite.setEmail("outra@example.com");
        assertThrows(IllegalArgumentException.class, () -> service.aceitar(token, aluno.getEmail()));
        assertTrue(turma.getUsuarios().isEmpty()); verify(convites, never()).delete(any());
    }
    @Test void expiradoNaoPodeAceitar() {
        convite.setExpiracao(LocalDateTime.now().minusMinutes(1));
        assertThrows(IllegalArgumentException.class, () -> service.aceitar(token, aluno.getEmail()));
        verify(convites, never()).delete(any());
    }
    @Test void conviteConsumidoNaoPodeReutilizar() {
        when(convites.buscarParaAceitar(token)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.aceitar(token, aluno.getEmail()));
        assertTrue(turma.getUsuarios().isEmpty());
    }
    @Test void membroExistenteNaoDuplica() {
        turma.addUsuario(aluno); service.aceitar(token, aluno.getEmail());
        assertEquals(1, turma.getUsuarios().size()); verify(convites).delete(convite);
    }
    @Test void perfilProfessorNaoPodeAceitar() {
        aluno.setPerfil("PROFESSOR");
        assertThrows(IllegalArgumentException.class, () -> service.aceitar(token, aluno.getEmail()));
    }
    @Test void contaDesativadaNaoPodeAceitar() {
        aluno.setAtivado(false);
        assertThrows(IllegalArgumentException.class, () -> service.aceitar(token, aluno.getEmail()));
    }
    @Test void tokenNumericoAntigoERejeitado() {
        assertThrows(IllegalArgumentException.class, () -> service.consultar("123", aluno.getEmail()));
        verify(convites, never()).findByToken("123");
    }
    @Test void criarNormalizaEmailEGeraToken() {
        when(convites.save(any())).thenAnswer(i -> i.getArgument(0));
        ConviteTurma criado = service.criarConvite(2L, " ALUNO@EXAMPLE.COM ");
        assertEquals("aluno@example.com", criado.getEmail());
        assertNotNull(UUID.fromString(criado.getToken()));
        assertTrue(criado.getExpiracao().isAfter(LocalDateTime.now().plusDays(6)));
        assertThrows(IllegalArgumentException.class, () -> service.criarConvite(2L, "sem-email"));
    }
}
