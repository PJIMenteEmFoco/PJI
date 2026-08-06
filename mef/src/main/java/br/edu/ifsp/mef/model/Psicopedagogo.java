package br.edu.ifsp.mef.model;

import java.time.LocalDate;

public class Psicopedagogo extends Usuario{
	
	private Long idInstituicao;
	private LocalDate dataNasci;
	
	public Long getIdInstituicao() {
		return idInstituicao;
	}

	public void setIdInstituicao(Long idInstituicao) {
		this.idInstituicao = idInstituicao;
	}

	public LocalDate getDataNasci() {
		return dataNasci;
	}

	public void setDataNasci(LocalDate dataNasci) {
		this.dataNasci = dataNasci;
	}
}
