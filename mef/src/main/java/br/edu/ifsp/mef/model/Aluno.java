package br.edu.ifsp.mef.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Aluno extends Usuario {

	private String desregulacao;
	private String hiperfoco;
	private float tempoFoco;
	private String laudo;
	@ManyToMany(mappedBy = "alunos")
	private Set<Atividade> atividades = new HashSet<>();
	@OneToOne(mappedBy = "aluno")
	private Calendario calendario;


	public String getDesregulacao() {
		return desregulacao;
	}

	public void setDesregulacao(String desregulacao) {
		this.desregulacao = desregulacao;
	}

	public String getHiperfoco() {
		return hiperfoco;
	}

	public void setHiperfoco(String hiperfoco) {
		this.hiperfoco = hiperfoco;
	}

	public float getTempoFoco() {
		return tempoFoco;
	}

	public void setTempoFoco(float tempoFoco) {
		this.tempoFoco = tempoFoco;
	}

	public String getLaudo() {
		return laudo;
	}

	public void setLaudo(String laudo) {
		this.laudo = laudo;
	}
	

	public Set<Atividade> getAtividades() {
	    return atividades;
	}

	public void setAtividades(Set<Atividade> atividades) {
	    this.atividades = atividades;
	}
}
