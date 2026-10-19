package br.edu.ifsp.mef.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.StatusAtividade;
import br.edu.ifsp.mef.model.TipoAtividade;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.AtividadeRepository;
import br.edu.ifsp.mef.repository.UsuarioRepository;

@Service
public class AtividadeService {

	@Autowired
	AtividadeRepository atividadeRepository;
	@Autowired
	UsuarioRepository usuarioRepository;

	private static final int JANELA_DIAS = 30;
	private static final int MINIMO_SEGUIDAS = 2;
	private static final int LIMITE_CONSULTA = 10;

	public void cadastrarAtividade(Atividade atividade) {

		Optional<Atividade> atividadeExistente = atividadeRepository.findByNomeIgnoreCase(atividade.getNome());

		if (atividadeExistente.isPresent()) {
			return;
		}

		Atividade novaAtividade = new Atividade();

		novaAtividade.setNome(atividade.getNome());
		novaAtividade.setPrazo(atividade.getPrazo());
		novaAtividade.setCapa(atividade.getCapa());
		novaAtividade.setDescricao(atividade.getDescricao());
		novaAtividade.setTipoAtividade(atividade.getTipoAtividade());
		novaAtividade.setAlunos(atividade.getAlunos());

		atividadeRepository.save(novaAtividade);
	}

	public Atividade getById(long id) {
		Optional<Atividade> byId = atividadeRepository.findById(id);
		if (byId.isEmpty()) {
			return null;
		}
		return byId.get();
	}

	public boolean alterarStatus(int status, Long id) {
		try {
			Atividade atividade = getById(id);
			atividade.setStatus(converterStatus(status));
			atividadeRepository.save(atividade);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean atualizarAtividade(Long id, String nome, LocalDate prazo, String descricao, int tipoAtividade) {
		try {
			Atividade atividade = getById(id);
			atividade.setNome(nome);
			atividade.setPrazo(prazo);
			atividade.setDescricao(descricao);
			atividade.setTipoAtividade(converterTipoAtividade(tipoAtividade));
			atividadeRepository.save(atividade);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private String rotulo(TipoAtividade tipo) {
		return tipo.name().replace('_', ' ').toLowerCase();
	}
	
	public List<String> avisosDeRepeticao(Long professorId, TipoAtividade tipo, List<Long> alunoIds) {
		if (alunoIds == null || alunoIds.isEmpty()) {
			return List.of();
		}

		LocalDateTime desde = LocalDateTime.now().minusDays(JANELA_DIAS);
		List<String> avisos = new ArrayList<>();

		for (Usuario aluno : usuarioRepository.findAllById(alunoIds)) {
			List<Atividade> ultimas = atividadeRepository.ultimasDoAluno(aluno.getId(), professorId, desde,
					PageRequest.of(0, LIMITE_CONSULTA));

			int seguidas = 0;
			for (Atividade atividade : ultimas) {
				if (atividade.getTipoAtividade() != tipo) {
					break;
				}
				seguidas++;
			}

			if (seguidas >= MINIMO_SEGUIDAS) {
				avisos.add(aluno.getNome() + ": as últimas " + seguidas + " atividades dos últimos " + JANELA_DIAS
						+ " dias já foram do tipo " + rotulo(tipo) + ". Esta seria a " + (seguidas + 1) + "ª seguida.");
			}
		}
		return avisos;
	}

	public StatusAtividade converterStatus(int num) {
		switch (num) {
		case 1: {
			return StatusAtividade.NAO_VINCULADO;
		}
		case 2: {
			return StatusAtividade.NAO_ACESSADO;
		}
		case 3: {
			return StatusAtividade.EM_ANDAMENTO;
		}
		case 4: {
			return StatusAtividade.ATRASADO;
		}
		case 5: {
			return StatusAtividade.CONCLUIDO;
		}
		default:
			throw new IllegalArgumentException("Valor inválido: " + num);
		}
	}

	public TipoAtividade converterTipoAtividade(int num) {
		switch (num) {
		case 1: {
			return TipoAtividade.JOGO_DA_MEMORIA;
		}
		case 2: {
			return TipoAtividade.CRUZADINHA;
		}
		case 3: {
			return TipoAtividade.QUIZ;
		}
		case 4: {
			return TipoAtividade.LEITURA_GUIADA;
		}
		default:
			throw new IllegalArgumentException("Valor inválido: " + num);
		}
	}

}
