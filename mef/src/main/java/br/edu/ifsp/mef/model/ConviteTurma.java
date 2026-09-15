package br.edu.ifsp.mef.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class ConviteTurma {

	@Id
	@GeneratedValue
	private Long id;
	private Long idTurma;	
	private LocalDateTime expiracao;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getIdTurma() {
		return idTurma;
	}
	public void setIdTurma(Long turmaId) {
		this.idTurma = turmaId;
	}
	public LocalDateTime getExpiracao() {
		return expiracao;
	}
	public void setExpiracao(LocalDateTime expiracao) {
		this.expiracao = expiracao;
	}
	
	
}
