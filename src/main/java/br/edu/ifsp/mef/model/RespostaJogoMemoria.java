package br.edu.ifsp.mef.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

@Entity
public class RespostaJogoMemoria {

	@Id
	private Long id;

	@OneToOne
	@MapsId
	@JoinColumn(name = "idAtividade")
	private Atividade atividade;
	private boolean prazoCumprido;
	private int quantidadeErros;
	private String feedbackAluno;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFeedbackAluno() {
		return feedbackAluno;
	}
	public void setFeedbackAluno(String feedbackAluno) {
		this.feedbackAluno = feedbackAluno;
	}
	public Atividade getAtividade() {
		return atividade;
	}
	public void setAtividade(Atividade atividade) {
		this.atividade = atividade;
	}
	public boolean isPrazoCumprido() {
		return prazoCumprido;
	}
	public void setPrazoCumprido(boolean prazoCumprido) {
		this.prazoCumprido = prazoCumprido;
	}
	public int getQuantidadeErros() {
		return quantidadeErros;
	}
	public void setQuantidadeErros(int quantidadeErros) {
		this.quantidadeErros = quantidadeErros;
	}
	
	
}
