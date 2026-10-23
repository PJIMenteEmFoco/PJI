package br.edu.ifsp.mef.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PerguntaLeitura {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "idTrecho")
	private TrechoLeitura trecho;

	private String enunciado;

	private String respostaEsperada;

	private String criterios;

	private boolean aprovada;

	public PerguntaLeitura() {}

	public PerguntaLeitura(String enunciado, String respostaEsperada, String criterios) {
		this.enunciado = enunciado;
		this.respostaEsperada = respostaEsperada;
		this.criterios = criterios;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public TrechoLeitura getTrecho() {
		return trecho;
	}

	public void setTrecho(TrechoLeitura trecho) {
		this.trecho = trecho;
	}

	public String getEnunciado() {
		return enunciado;
	}

	public void setEnunciado(String enunciado) {
		this.enunciado = enunciado;
	}

	public String getRespostaEsperada() {
		return respostaEsperada;
	}

	public void setRespostaEsperada(String respostaEsperada) {
		this.respostaEsperada = respostaEsperada;
	}

	public String getCriterios() {
		return criterios;
	}

	public void setCriterios(String criterios) {
		this.criterios = criterios;
	}

	public boolean isAprovada() {
		return aprovada;
	}

	public void setAprovada(boolean aprovada) {
		this.aprovada = aprovada;
	}
	
}
