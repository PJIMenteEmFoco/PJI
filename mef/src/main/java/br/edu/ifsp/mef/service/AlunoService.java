package br.edu.ifsp.mef.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.ifsp.mef.model.Usuario;
import br.edu.ifsp.mef.repository.TurmaRepository;

@Service
public class AlunoService {
	
	@Autowired
	TurmaRepository turmaRepository;
	
	public List<Usuario> buscarAlunosTurma(Long idAluno) {
		return turmaRepository.buscarProfessoresDoAluno(idAluno);
	}

}
