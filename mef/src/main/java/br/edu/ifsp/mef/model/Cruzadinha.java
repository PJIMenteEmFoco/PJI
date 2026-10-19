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
public class Cruzadinha {

	@Id
	private Long id;

	@OneToOne
	@MapsId
	@JoinColumn(name = "idAtividade")
	private Atividade atividade;

	private int linhas;
	private int colunas;

	@OneToMany(mappedBy = "cruzadinha", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PalavraCruzada> palavrasCruzadas = new ArrayList<>();

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

	public int getLinhas() {
		return linhas;
	}

	public void setLinhas(int linhas) {
		this.linhas = linhas;
	}

	public int getColunas() {
		return colunas;
	}

	public void setColunas(int colunas) {
		this.colunas = colunas;
	}

	public List<PalavraCruzada> getPalavraCruzada() {
		return palavrasCruzadas;
	}

	public void setPalavraCruzada(List<PalavraCruzada> palavraCruzadas) {
		this.palavrasCruzadas = palavraCruzadas;
	}

	public void addPalavra(PalavraCruzada p) {
		palavrasCruzadas.add(p);
		p.setCruzadinha(this);
	}
	
}

