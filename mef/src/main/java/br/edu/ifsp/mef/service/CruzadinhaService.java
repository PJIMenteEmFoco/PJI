package br.edu.ifsp.mef.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import br.edu.ifsp.mef.model.Atividade;
import br.edu.ifsp.mef.model.Cruzadinha;
import br.edu.ifsp.mef.model.Direcao;
import br.edu.ifsp.mef.model.PalavraCruzada;
import br.edu.ifsp.mef.repository.CruzadinhaRepository;

public class CruzadinhaService {

	@Autowired
	CruzadinhaRepository cruzadinhaRepository;
	
	public Cruzadinha getById(Long id) {
		Optional<Cruzadinha> byId = cruzadinhaRepository.findById(id);
		if(byId.isEmpty()) {
			return null;
		}
		return byId.get();
	}
	
	public boolean atualizarCruzadinha(Long id, Atividade atividade, int linhas, int colunas, List<PalavraCruzada> palavras) {
		try {
			Cruzadinha cruzadinha = getById(id);
			cruzadinha.setAtividade(atividade);;
			cruzadinha.setLinhas(linhas);;
			cruzadinha.setColunas(colunas);;
			cruzadinha.setPalavraCruzada(palavras);
			cruzadinhaRepository.save(cruzadinha);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public char[][] montarGrade(Long id) {
		Cruzadinha cruzadinha = getById(id);
		
		char[][] grade = new char[cruzadinha.getLinhas()][cruzadinha.getColunas()]; 

		for (PalavraCruzada p : cruzadinha.getPalavraCruzada()) {
			for (int i = 0; i < p.getResposta().length(); i++) {
				int l = p.getLinha() + (p.getDirecao() == Direcao.VERTICAL ? i : 0);
				int c = p.getColuna() + (p.getDirecao() == Direcao.HORIZONTAL ? i : 0);

				if (l >= cruzadinha.getLinhas() || c >= cruzadinha.getColunas()) {
					throw new IllegalArgumentException("A palavra " + p.getResposta() + " ultrapassa a grade.");
				}
;
				char letra = p.getResposta().charAt(i);
				if (grade[l][c] != '\0' && grade[l][c] != letra) {
					throw new IllegalArgumentException("Conflito de letras em " + p.getResposta() + " (linha " + l + ", coluna " + c + ").");
				}
				grade[l][c] = letra;
			}
		}
		return grade;
	}
}
