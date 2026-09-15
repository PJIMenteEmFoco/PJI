package br.edu.ifsp.mef.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

@Entity
public class ConfiguracoesUsuario {
	
	@Id
	private Long id;
	
	@OneToOne
	@MapsId
	@JoinColumn(name = "idUsuario")
	private Usuario usuario;
	private boolean notificacao;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Usuario getUsuario() {
		return usuario;
	}
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	public boolean isNotificacao() {
		return notificacao;
	}
	public void setNotificacao(boolean notificacao) {
		this.notificacao = notificacao;
	}
	
	
	
}
