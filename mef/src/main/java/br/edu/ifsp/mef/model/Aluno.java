package br.edu.ifsp.mef.model;

import java.time.LocalDate;

public class Aluno extends Usuario {

	private LocalDate dataNasci;

	public LocalDate getDataNasci() {
		return dataNasci;
	}

	public void setDataNasci(LocalDate dataNasci) {
		this.dataNasci = dataNasci;
	}
}
