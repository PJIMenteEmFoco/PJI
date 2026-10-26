package br.edu.ifsp.mef.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Calendario;
import br.edu.ifsp.mef.model.Evento;
import br.edu.ifsp.mef.model.Feriado;
import br.edu.ifsp.mef.model.PrioridadeEvento;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.service.AlunoService;
import br.edu.ifsp.mef.service.CalendarioService;
import br.edu.ifsp.mef.service.FeriadoService;

@RestController
@RequestMapping("/aluno/api/calendario")
public class AlunoCalendarioApiController {

    private final AlunoService alunoService;
    private final CalendarioService calendarioService;
    private final FeriadoService feriadoService;

    public AlunoCalendarioApiController(
            AlunoService alunoService,
            CalendarioService calendarioService,
            FeriadoService feriadoService) {
        this.alunoService = alunoService;
        this.calendarioService = calendarioService;
        this.feriadoService = feriadoService;
    }

    @PostMapping("/eventos")
    public ResponseEntity<?> criarEvento(@RequestBody EventoRequest request, Authentication authentication) {
        Aluno aluno = alunoAutenticado(authentication);
        if (aluno == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErroResposta("Esta operação está disponível apenas para alunos."));
        }

        if (request == null || request.titulo() == null || request.titulo().isBlank() || request.data() == null) {
            return ResponseEntity.badRequest()
                    .body(new ErroResposta("Informe o título e a data do evento."));
        }

        PrioridadeEvento prioridade;
        try {
            prioridade = converterPrioridade(request.prioridade());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(new ErroResposta("A prioridade informada não é válida."));
        }

        Calendario calendario = calendarioService.buscarOpcionalPorAluno(aluno)
                .orElseGet(() -> calendarioService.criarCalendario(aluno));

        Evento evento = new Evento();
        evento.setNome(request.titulo().trim());
        evento.setDescricao(request.descricao() == null ? "" : request.descricao().trim());
        evento.setDataInicio(request.data());
        evento.setDataFim(request.data());
        evento.setHorario(request.horario() == null ? LocalTime.of(14, 0) : request.horario());
        evento.setPrioridade(prioridade);

        Evento salvo = calendarioService.adicionarEvento(calendario.getId(), evento);
        return ResponseEntity.status(HttpStatus.CREATED).body(new EventoResposta(
                salvo.getId(), salvo.getNome(), salvo.getDescricao(), salvo.getDataInicio(),
                salvo.getHorario(), salvo.getPrioridade().name()));
    }

    @GetMapping("/feriados/{ano}")
    public ResponseEntity<?> buscarFeriados(@PathVariable int ano, Authentication authentication) {
        if (alunoAutenticado(authentication) == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErroResposta("Esta operação está disponível apenas para alunos."));
        }
        if (ano < 1900 || ano > 2200) {
            return ResponseEntity.badRequest().body(new ErroResposta("O ano solicitado não é válido."));
        }

        List<Feriado> feriados;
        try {
            feriados = feriadoService.buscarOuSincronizarAno(ano);
        } catch (RuntimeException exception) {
            // A página ainda funciona se a API externa estiver indisponível.
            feriados = feriadoService.buscarEntre(LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31));
        }

        List<FeriadoResposta> resposta = feriados.stream()
                .map(feriado -> new FeriadoResposta(feriado.getDataFeriado().toString(), feriado.getNome()))
                .toList();
        return ResponseEntity.ok(resposta);
    }

    private Aluno alunoAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Usuario usuario = alunoService.getByEmail(authentication.getName());
        return usuario instanceof Aluno aluno ? aluno : null;
    }

    private PrioridadeEvento converterPrioridade(String valor) {
        if (valor == null || valor.isBlank()) return PrioridadeEvento.MEDIA;
        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "alta", "high" -> PrioridadeEvento.ALTA;
            case "media", "média", "medium" -> PrioridadeEvento.MEDIA;
            case "baixa", "low" -> PrioridadeEvento.BAIXA;
            default -> throw new IllegalArgumentException("Prioridade desconhecida");
        };
    }

    public record EventoRequest(String titulo, String descricao, LocalDate data, LocalTime horario, String prioridade) {}
    public record EventoResposta(Long id, String nome, String descricao, LocalDate dataInicio,
                                 LocalTime horario, String prioridade) {}
    public record FeriadoResposta(String data, String titulo) {}
    public record ErroResposta(String message) {}
}
