package br.edu.ifsp.mef.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.mef.model.Aluno;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.AlunoRepository;
import br.edu.ifsp.mef.repository.AtividadeRepository;
import br.edu.ifsp.mef.repository.TurmaRepository;

@Service
public class AlunoService {

	@Autowired
	TurmaRepository turmaRepository;
	@Autowired
	AtividadeRepository atividadeRepository;
	@Autowired
	AlunoRepository alunoRepository;
	@Autowired
	NivelService nivelService;

	public List<Usuario> buscarAlunosTurma(Long idAluno) {
		return turmaRepository.buscarProfessoresDoAluno(idAluno);
	}

    public List<Usuario> buscarContatosChat(Long idAluno) {
        return turmaRepository.buscarContatosChatDoAluno(idAluno);
    }

	public List<Atividade> buscarAtividades(Long idAluno) {
		return atividadeRepository.findAtividadesDoAluno(idAluno);
	}

	public Aluno getById(long id) {
		Optional<Aluno> byId = alunoRepository.findById(id);
		if (byId.isEmpty()) {
			return null;
		}
		return byId.get();
	}

	public Usuario getByEmail(String email) {
		Optional<Aluno> byEmail = alunoRepository.findByEmailIgnoreCase(email);
		if (byEmail.isEmpty()) {
			return null;
		}
		return byEmail.get();
	}
	
	@Transactional
	public boolean receberXp(Long idAluno, double xp) {
		if (idAluno == null || !Double.isFinite(xp) || xp <= 0) {
			throw new IllegalArgumentException("Aluno e quantidade de XP válidos são obrigatórios.");
		}
		Aluno aluno = getById(idAluno);
		if (aluno == null) {
			throw new IllegalArgumentException("Aluno não encontrado para receber XP.");
		}

		// addXp trata XP inicial nulo como zero.
		aluno.addXp(xp);
		double xpTotal = aluno.getXp() == null ? 0.0 : aluno.getXp();
		boolean subiuNivel = false;

		// A quantidadeXp do nível representa o limiar acumulado para avançar.
		// Pode haver mais de um nível alcançado se o total acumulado já ultrapassar vários limiares.
		br.edu.ifsp.mef.model.Nivel nivelAtual = aluno.getNivel();
		while (nivelAtual != null && nivelAtual.getId() != null
				&& nivelAtual.getQuantidadeXp() != null
				&& xpTotal >= nivelAtual.getQuantidadeXp()) {
			br.edu.ifsp.mef.model.Nivel proximo = nivelService.getById(nivelAtual.getId() + 1);
			if (proximo == null || proximo.getId() == null || proximo.getId().equals(nivelAtual.getId())) {
				break; // O aluno já chegou ao nível máximo cadastrado.
			}
			aluno.setNivel(proximo);
			nivelAtual = proximo;
			subiuNivel = true;
		}

		alunoRepository.save(aluno);
		return subiuNivel;
	}
	
}
