package br.edu.ifsp.mef.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Professor extends Usuario {
	
	/*
	@ManyToOne
	@JoinColumn(name = "id_instituicao")
	private Instituicao instituicao;
	
	public Instituicao getInstituicao() {
	    return instituicao;
	}

	public void setInstituicao(Instituicao instituicao) {
	    this.instituicao = instituicao;
	}
*/
}
