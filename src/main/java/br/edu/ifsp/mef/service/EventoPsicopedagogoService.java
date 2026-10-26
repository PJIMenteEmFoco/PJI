package br.edu.ifsp.mef.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.mef.model.EventoPsicopedagogo;
import br.edu.ifsp.mef.model.PrioridadeEvento;
import br.edu.ifsp.mef.model.Psicopedagogo;
import br.edu.ifsp.mef.repository.EventoPsicopedagogoRepository;
import br.edu.ifsp.mef.repository.UsuarioRepository;

@Service
public class EventoPsicopedagogoService {

    private final EventoPsicopedagogoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoPsicopedagogoService(EventoPsicopedagogoRepository eventoRepository,
            UsuarioRepository usuarioRepository) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<EventoPsicopedagogo> listarDoPsicopedagogo(Long psicopedagogoId) {
        return eventoRepository.findByPsicopedagogo_IdOrderByDataEventoAscHorarioAsc(psicopedagogoId);
    }

    @Transactional
    public EventoPsicopedagogo criar(Long psicopedagogoId, String titulo, LocalDate dataEvento,
            LocalTime horario, PrioridadeEvento prioridade, String descricao) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Informe o título do evento.");
        }
        String tituloTratado = titulo.trim();
        if (tituloTratado.length() > 120) {
            throw new IllegalArgumentException("O título pode ter no máximo 120 caracteres.");
        }
        if (dataEvento == null) {
            throw new IllegalArgumentException("Informe a data do evento.");
        }
        if (horario == null) {
            throw new IllegalArgumentException("Informe o horário do evento.");
        }

        Psicopedagogo psicopedagogo = usuarioRepository.findById(psicopedagogoId)
                .filter(usuario -> usuario instanceof Psicopedagogo)
                .map(usuario -> (Psicopedagogo) usuario)
                .orElseThrow(() -> new AccessDeniedException("Apenas psicopedagogos podem criar eventos."));

        EventoPsicopedagogo evento = new EventoPsicopedagogo();
        evento.setPsicopedagogo(psicopedagogo);
        evento.setTitulo(tituloTratado);
        evento.setDataEvento(dataEvento);
        evento.setHorario(horario);
        evento.setPrioridade(prioridade == null ? PrioridadeEvento.MEDIA : prioridade);

        String texto = descricao == null ? "" : descricao.trim();
        evento.setDescricao(texto.length() <= 1000 ? texto : texto.substring(0, 1000));

        return eventoRepository.save(evento);
    }
}
