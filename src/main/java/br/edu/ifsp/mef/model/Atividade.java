package br.edu.ifsp.mef.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.JoinColumn;

@Entity
public class Atividade {

	@Id
	@GeneratedValue
	private Long id;
	private String nome;
	private LocalDate prazo;
	private String capa;
	private String descricao;
	private int tempoAtividade;
	private LocalDateTime dataCriacao;

	@PrePersist
	void definirDataCriacao() {
		if (dataCriacao == null) {
			dataCriacao = LocalDateTime.now();
		}
	}

	@Enumerated(EnumType.STRING)
	private TipoAtividade tipoAtividade;
	@Enumerated(EnumType.STRING)
	private StatusAtividade status;
	@ManyToMany
	@JoinTable(name = "atividade_aluno", joinColumns = @JoinColumn(name = "idAtividade"), inverseJoinColumns = @JoinColumn(name = "idAluno"))
	private Set<Aluno> alunos = new HashSet<>();
	@ElementCollection
	@CollectionTable(name = "atividade_rascunho_destinatario", joinColumns = @JoinColumn(name = "id_atividade"))
	@Column(name = "id_aluno", nullable = false)
	private Set<Long> destinatariosRascunho = new HashSet<>();

	public Set<Long> getDestinatariosRascunho() {
		return destinatariosRascunho;
	}

	public void setDestinatariosRascunho(Set<Long> ids) {
		this.destinatariosRascunho = ids;
	}

	@ManyToMany(mappedBy = "atividades")
	private Set<Calendario> calendarios = new HashSet<>();
	@ManyToOne
	@JoinColumn(name = "idProfessor")
	private Professor professor;
	@ManyToOne
	@JoinColumn(name = "idPsicopedagogo")
	private Psicopedagogo psicopedagogo;

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

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public Psicopedagogo getPsicopedagogo() {
		return psicopedagogo;
	}

	public void setPsicopedagogo(Psicopedagogo psicopedagogo) {
		this.psicopedagogo = psicopedagogo;
	}

	public int getTempoAtividade() {
		return tempoAtividade;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public void setTempoAtividade(int tempoAtividade) {
		this.tempoAtividade = tempoAtividade;
	}

	public TipoAtividade getTipoAtividade() {
		return tipoAtividade;
	}

	public void setTipoAtividade(TipoAtividade tipoAtividade) {
		this.tipoAtividade = tipoAtividade;
	}

	public StatusAtividade getStatus() {
		return status;
	}

	public void setStatus(StatusAtividade status) {
		this.status = status;
	}

	public Set<Aluno> getAlunos() {
		return alunos;
	}

	public void setAlunos(Set<Aluno> alunos) {
		this.alunos = alunos;
	}

	public void addAluno(Aluno aluno) {
		this.alunos.add(aluno);
		aluno.getAtividades().add(this);
	}

	public void removeAluno(Aluno aluno) {
		this.alunos.remove(aluno);
		aluno.getAtividades().remove(this);
	}

	public Set<Calendario> getCalendarios() {
		return calendarios;
	}

	public void setCalendarios(Set<Calendario> calendarios) {
		this.calendarios = calendarios;
	}

}
