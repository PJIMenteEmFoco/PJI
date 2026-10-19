package br.edu.ifsp.mef.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class PalavraCruzada {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "idCruzadinha")
	private Cruzadinha cruzadinha;

	private String dica;
	private String resposta;
	
	//posicao das palavras na cruzadinha, se comporta como uma matriz, inicia em 0
	private int linha;     
	private int coluna;

	@Enumerated(EnumType.STRING)
	private Direcao direcao;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Cruzadinha getCruzadinha() {
		return cruzadinha;
	}

	public void setCruzadinha(Cruzadinha cruzadinha) {
		this.cruzadinha = cruzadinha;
	}

	public String getDica() {
		return dica;
	}

	public void setDica(String dica) {
		this.dica = dica;
	}

	public String getResposta() {
		return resposta;
	}

	public void setResposta(String resposta) {
		this.resposta = resposta;
	}

	public int getLinha() {
		return linha;
	}

	public void setLinha(int linha) {
		this.linha = linha;
	}

	public int getColuna() {
		return coluna;
	}

	public void setColuna(int coluna) {
		this.coluna = coluna;
	}

	public Direcao getDirecao() {
		return direcao;
	}

	public void setDirecao(Direcao direcao) {
		this.direcao = direcao;
	}
	
	

	

}
