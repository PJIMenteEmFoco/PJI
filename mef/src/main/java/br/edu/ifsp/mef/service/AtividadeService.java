package br.edu.ifsp.mef.service;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.StatusAtividade;
import br.edu.ifsp.mef.model.TipoAtividade;
import br.edu.ifsp.mef.repository.AtividadeRepository;

@Service
public class AtividadeService {

	@Autowired
	AtividadeRepository atividadeRepository;
	
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
		novaAtividade.setArquivoAtividade(atividade.getArquivoAtividade());

	    atividadeRepository.save(novaAtividade);
	}
	
	    
		public Atividade getById(long id) {
			Optional<Atividade> byId = atividadeRepository.findById(id);
			if(byId.isEmpty()) {
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
				return TipoAtividade.PALAVRA_CRUZADA;
			}
			case 3: {
				return TipoAtividade.QUIZ;
			}
			default:
				throw new IllegalArgumentException("Valor inválido: " + num);
			}
		}
	
}
