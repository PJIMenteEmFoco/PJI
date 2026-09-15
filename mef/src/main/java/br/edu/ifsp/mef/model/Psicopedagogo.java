package br.edu.ifsp.mef.model;

import jakarta.persistence.Entity;

@Entity
public class Psicopedagogo extends Usuario {
	
	private String caminhoComprovante;

	public String getCaminhoComprovante() {
		return caminhoComprovante;
	}

	public void setCaminhoComprovante(String caminhoConprovante) {
		this.caminhoComprovante = caminhoConprovante;
	}
	
	

}
