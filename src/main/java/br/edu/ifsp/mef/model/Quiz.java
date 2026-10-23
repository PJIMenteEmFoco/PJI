package br.edu.ifsp.mef.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Quiz {

	@Id
	private Long id;
	@OneToOne
	@MapsId
	@JoinColumn(name = "idAtividade")
	private Atividade atividade;
	@OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
	List<Pergunta> perguntas = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Atividade getAtividade() {
		return atividade;
	}

	public void setAtividade(Atividade atividade) {
		this.atividade = atividade;
	}

	public List<Pergunta> getPerguntas() {
		return perguntas;
	}

	public void setPerguntas(List<Pergunta> perguntas) {
		this.perguntas = perguntas;
	}

	public void addPergunta(Pergunta pergunta) {
		perguntas.add(pergunta);
		pergunta.setQuiz(this);
	}

}
