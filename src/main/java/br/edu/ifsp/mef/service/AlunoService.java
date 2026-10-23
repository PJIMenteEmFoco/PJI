package br.edu.ifsp.mef.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	
	public boolean receberXp(Long idAluno, double xp) {
		Aluno aluno = getById(idAluno);
		aluno.addXp(xp);
		if (aluno.getXp() >= aluno.getNivel().getQuantidadeXp()) {
			aluno.setNivel(nivelService.getById((aluno.getNivel().getId()+1)));
			return true;
		} else {
			return false;
		}
		
	}
	
}
