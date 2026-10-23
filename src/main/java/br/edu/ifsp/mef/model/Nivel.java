package br.edu.ifsp.mef.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Nivel {

	@Id
	private Long id;
	private String nome;
	private Double quantidadeXp;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public Double getQuantidadeXp() {
		return quantidadeXp;
	}
	public void setQuantidadeXp(Double quantidadeXp) {
		this.quantidadeXp = quantidadeXp;
	}
	
	
}
