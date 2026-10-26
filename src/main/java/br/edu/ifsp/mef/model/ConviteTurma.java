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
	private String email;
	private Long idTurma;
	private String token;
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
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
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
