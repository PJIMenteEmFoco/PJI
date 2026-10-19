package br.edu.ifsp.mef.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Aluno extends Usuario {

	private String desregulacao;
	private String hiperfoco;
	private float tempoFoco;
	private String laudo;
	private Double xp;
	@ManyToOne
	@JoinColumn(name="idNivel")
	private Nivel nivel;
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

	public Double getXp() {
		return xp;
	}

	public void setXp(Double xp) {
		this.xp = xp;
	}
	
	public void addXp(Double xp) {
		this.xp += xp;
	}

	public Nivel getNivel() {
		return nivel;
	}

	public void setNivel(Nivel nivel) {
		this.nivel = nivel;
	}

	public Calendario getCalendario() {
		return calendario;
	}

	public void setCalendario(Calendario calendario) {
		this.calendario = calendario;
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
