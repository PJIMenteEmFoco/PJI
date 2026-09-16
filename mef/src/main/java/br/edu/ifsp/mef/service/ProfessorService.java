package br.edu.ifsp.mef.service;

import br.edu.ifsp.mef.repository.AtividadeRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.TurmaRepository;

@Service
public class ProfessorService {
	@Autowired
	AtividadeRepository atividadeRepository;
	@Autowired
	TurmaRepository turmaRepository;

	public long contarAlunos(Long idProfessor) {
		return turmaRepository.countAlunosByIdProfessor(idProfessor);
	}

	public List<Usuario> buscarAlunosTurma(Long idProfessor) {
		return turmaRepository.buscarAlunosDoProfessor(idProfessor);
	}

	public long contarAtividadesDaSemana(Long idProfessor) {

		LocalDate hoje = LocalDate.now();

		LocalDate inicioSemana = hoje.with(DayOfWeek.MONDAY);

		LocalDate fimSemana = inicioSemana.plusDays(6);

		return atividadeRepository.countAtividadesDaSemanaDoProfessor(idProfessor, inicioSemana, fimSemana);
	}

	public List<Atividade> buscarAtividadesDaSemana(Long idProfessor) {

		LocalDate hoje = LocalDate.now();

		LocalDate inicioSemana = hoje.with(java.time.DayOfWeek.MONDAY);

		LocalDate fimSemana = inicioSemana.plusDays(6);

		return atividadeRepository.findAtividadesDaSemanaDoProfessor(idProfessor, inicioSemana, fimSemana);
	}

}
