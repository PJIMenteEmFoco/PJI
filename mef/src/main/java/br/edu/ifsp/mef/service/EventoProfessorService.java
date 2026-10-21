package br.edu.ifsp.mef.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.mef.model.EventoProfessor;
import br.edu.ifsp.mef.model.PrioridadeEvento;
import br.edu.ifsp.mef.model.Professor;
import br.edu.ifsp.mef.repository.EventoProfessorRepository;
import br.edu.ifsp.mef.repository.UsuarioRepository;

@Service
public class EventoProfessorService {

	private final EventoProfessorRepository eventoRepository;
	private final UsuarioRepository usuarioRepository;

	public EventoProfessorService(EventoProfessorRepository eventoRepository, UsuarioRepository usuarioRepository) {
		this.eventoRepository = eventoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional(readOnly = true)
	public List<EventoProfessor> listarDoProfessor(Long professorId) {
		return eventoRepository.findByProfessorIdOrderByDataEventoAscHorarioAsc(professorId);
	}

	@Transactional
	public EventoProfessor criar(Long professorId, String titulo, LocalDate dataEvento, LocalTime horario,
			PrioridadeEvento prioridade, String descricao) {

		// A validação do navegador ajuda, mas só vale a do servidor.
		if (titulo == null || titulo.isBlank()) {
			throw new IllegalArgumentException("Informe o título do evento.");
		}
		if (titulo.trim().length() > 120) {
			throw new IllegalArgumentException("O título pode ter no máximo 120 caracteres.");
		}
		if (dataEvento == null) {
			throw new IllegalArgumentException("Informe a data do evento.");
		}

		// O professor sempre vem do login (nunca de um campo do formulário).
		Professor professor = usuarioRepository.findById(professorId)
				.filter(u -> u instanceof Professor)
				.map(u -> (Professor) u)
				.orElseThrow(() -> new AccessDeniedException("Apenas professores podem criar eventos."));

		EventoProfessor evento = new EventoProfessor();
		evento.setProfessor(professor);
		evento.setTitulo(titulo.trim());
		evento.setDataEvento(dataEvento);
		evento.setHorario(horario);
		evento.setPrioridade(prioridade == null ? PrioridadeEvento.MEDIA : prioridade);

		String texto = descricao == null ? "" : descricao.trim();
		evento.setDescricao(texto.length() <= 1000 ? texto : texto.substring(0, 1000));

		return eventoRepository.save(evento);
	}
}