package br.edu.ifsp.mef.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.Calendario;
import br.edu.ifsp.mef.model.Evento;
import br.edu.ifsp.mef.repository.AtividadeRepository;
import br.edu.ifsp.mef.repository.CalendarioRepository;
import br.edu.ifsp.mef.repository.EventoRepository;

@Service
public class CalendarioService {

	@Autowired
	CalendarioRepository calendarioRepository;
	EventoRepository eventoRepository;
	AtividadeRepository atividadeRepository;

	public CalendarioService() {
	}

	public Calendario criarCalendario(Aluno aluno) {

		if (calendarioRepository.findByAluno(aluno).isPresent()) {
			throw new RuntimeException("O aluno já possui um calendário.");
		}

		Calendario calendario = new Calendario();
		calendario.setAluno(aluno);

		return calendarioRepository.save(calendario);
	}

	public Calendario buscarPorId(Long id) {

		return calendarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Calendário não encontrado."));
	}

	public Calendario buscarPorAluno(Aluno aluno) {

		return calendarioRepository.findByAluno(aluno).orElseThrow(() -> new RuntimeException("Calendário do aluno não encontrado."));
	}

	@Transactional
	public Evento adicionarEvento(Long idCalendario, Evento evento) {

		Calendario calendario = buscarPorId(idCalendario);

		evento.setCalendario(calendario);

		calendario.getEventos().add(evento);

		return eventoRepository.save(evento);
	}

	@Transactional
	public Evento atualizarEvento(Long idEvento, Evento dados) {

		Evento evento = eventoRepository.findById(idEvento).orElseThrow(() -> new RuntimeException("Evento não encontrado."));

		evento.setNome(dados.getNome());
		evento.setDescricao(dados.getDescricao());
		evento.setDataInicio(dados.getDataInicio());
		evento.setDataFim(dados.getDataFim());

		return eventoRepository.save(evento);
	}

	@Transactional
	public void removerEvento(Long idEvento) {

		Evento evento = eventoRepository.findById(idEvento).orElseThrow(() -> new RuntimeException("Evento não encontrado."));

		Calendario calendario = evento.getCalendario();

		if (calendario != null) {
			calendario.getEventos().remove(evento);
		}

		eventoRepository.delete(evento);
	}

	public List<Evento> listarEventos(Long idCalendario) {

		Calendario calendario = buscarPorId(idCalendario);

		return eventoRepository.findByCalendario(calendario);
	}

	@Transactional
	public void adicionarAtividade(Long idCalendario, Long idAtividade) {

		Calendario calendario = buscarPorId(idCalendario);

		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(() -> new RuntimeException("Atividade não encontrada."));

		calendario.addAtividade(atividade);

		calendarioRepository.save(calendario);
	}

	@Transactional
	public void removerAtividade(Long idCalendario, Long idAtividade) {

		Calendario calendario = buscarPorId(idCalendario);

		Atividade atividade = atividadeRepository.findById(idAtividade).orElseThrow(() -> new RuntimeException("Atividade não encontrada."));

		calendario.removerAtividade(atividade);

		calendarioRepository.save(calendario);
	}

    public List<Atividade> listarAtividades(Long idCalendario) {

        Calendario calendario = buscarPorId(idCalendario);

        return calendario.getAtividades().stream().toList();
    }
}
