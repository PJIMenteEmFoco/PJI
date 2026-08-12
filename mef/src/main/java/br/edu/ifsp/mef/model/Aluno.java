package br.edu.ifsp.mef.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Aluno extends Usuario {

	/*
	@ManyToOne
	@JoinColumn(name = "id_instituicao")
	private Instituicao instituicao;
	*/
	@ManyToOne
	@JoinColumn(name = "id_psicopedagogo")
	private Psicopedagogo psico;
	private String desregulacao;
	private String hiperfoco;
	private float tempoFoco;
	private String laudo;
	
	private LocalDate dataNasci;

	public LocalDate getDataNasci() {
		return dataNasci;
	}

	public void setDataNasci(LocalDate dataNasci) {
		this.dataNasci = dataNasci;
	}

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
	
	/*public Instituicao getInstituicao() {
	    return instituicao;
	}
	
	public void setInstituicao(Instituicao instituicao) {
	    this.instituicao = instituicao;
	}
	*/
	public Psicopedagogo getPsico() {
	    return psico;
	}
	
	public void setPsico(Psicopedagogo psico) {
	    this.psico = psico;
	}

}
