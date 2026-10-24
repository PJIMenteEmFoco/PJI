package br.edu.ifsp.mef.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class RelatorioPsicopedagogo {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_psicopedagogo")
	private Psicopedagogo psicopedagogo;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_aluno")
	private Aluno aluno;

	private String titulo;

	private String observacoes;

	private String estrategias;

	private String proximosPassos;

	private LocalDateTime dataCriacao;

	private LocalDateTime dataAtualizacao;

	@PrePersist
	protected void aoCriar() {
		if (dataCriacao == null)
			dataCriacao = LocalDateTime.now();
	}

	@PreUpdate
	protected void aoAtualizar() {
		dataAtualizacao = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Psicopedagogo getPsicopedagogo() {
		return psicopedagogo;
	}

	public void setPsicopedagogo(Psicopedagogo psicopedagogo) {
		this.psicopedagogo = psicopedagogo;
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public String getEstrategias() {
		return estrategias;
	}

	public void setEstrategias(String estrategias) {
		this.estrategias = estrategias;
	}

	public String getProximosPassos() {
		return proximosPassos;
	}

	public void setProximosPassos(String proximosPassos) {
		this.proximosPassos = proximosPassos;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public LocalDateTime getDataAtualizacao() {
		return dataAtualizacao;
	}
}
