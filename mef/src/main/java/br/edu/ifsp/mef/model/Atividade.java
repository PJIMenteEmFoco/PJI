package br.edu.ifsp.mef.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
	private String arquivoAtividade;
	@Enumerated(EnumType.STRING)
	private TipoAtividade tipoAtividade;
	@Enumerated(EnumType.STRING)
	private StatusAtividade status;
	@ManyToMany
	@JoinTable (
	  name = "atividade_aluno", 
	  joinColumns = @JoinColumn(name = "idAtividade"), 
	  inverseJoinColumns = @JoinColumn(name = "idAluno"))
	private Set<Aluno> alunos = new HashSet<>();
	@ManyToMany(mappedBy = "atividades")
	private Set<Calendario> calendarios = new HashSet<>();
	
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
	
	public String getArquivoAtividade() {
		return arquivoAtividade;
	}
	public void setArquivoAtividade(String arquivoAtividade) {
		this.arquivoAtividade = arquivoAtividade;
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
