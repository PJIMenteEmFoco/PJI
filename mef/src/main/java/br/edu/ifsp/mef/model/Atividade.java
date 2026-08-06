package br.edu.ifsp.mef.model;

import java.time.LocalDate;

public class Atividade {

	private Long id;
	private String nome;
	private LocalDate prazo;
	private String capa;
	private String descricao;
	private Long idTipoAtivade;
	private Long[] idAlunos;
	
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
	
	public LocalDate getPrazo() {
		return prazo;
	}
	public void setPrazo(LocalDate prazo) {
		this.prazo = prazo;
	}
	
	public String getCapa() {
		return capa;
	}
	public void setCapa(String capa) {
		this.capa = capa;
	}
	
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
	public Long getIdTipoAtivade() {
		return idTipoAtivade;
	}
	public void setIdTipoAtivade(Long idTipoAtivade) {
		this.idTipoAtivade = idTipoAtivade;
	}
	
	public Long[] getIdAlunos() {
		return idAlunos;
	}
	public void setIdAlunos(Long[] idAlunos) {
		this.idAlunos = idAlunos;
	}
}
