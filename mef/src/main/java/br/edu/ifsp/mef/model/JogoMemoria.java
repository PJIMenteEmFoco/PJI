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
public class JogoMemoria {

	@Id
	private Long id;
	@OneToOne
	@MapsId
	@JoinColumn(name = "idAtividade")
	private Atividade atividade;
	@OneToMany(mappedBy = "jogoMemoria", cascade = CascadeType.ALL)
	private List<ParCarta> pares = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public Atividade getAtividade() {
		return atividade;
	}

	public void setAtividade(Atividade atividade) {
		this.atividade = atividade;
	}

	public List<ParCarta> getPares() {
		return pares;
	}

	public void addPar(ParCarta par) {
		pares.add(par);
		par.setJogoMemoria(this);
	}
}