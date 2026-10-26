package br.edu.ifsp.mef.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Calendario {
	@Id
	@GeneratedValue
	private Long id;
	@OneToMany(mappedBy = "calendario")
	private List<Evento> eventos = new ArrayList<>();
	@OneToOne
	@JoinColumn(name = "idAluno", unique = true)
	private Aluno aluno;
	@ManyToMany
	@JoinTable (
	  name = "atividade_calendario", 
	  joinColumns = @JoinColumn(name = "idCalendario"), 
	  inverseJoinColumns = @JoinColumn(name = "idAtividade"))
	private Set<Atividade> atividades = new HashSet<>();
	
	
	public Set<Atividade> getAtividades() {
	    return atividades;
	}

	public void setAtividades(Set<Atividade> atividades) {
	    this.atividades = atividades;
	}
	
	public void addAtividade(Atividade atividade) {
	    this.atividades.add(atividade);
	    atividade.getCalendarios().add(this);
	}

	public void removerAtividade(Atividade atividade) {
	    this.atividades.remove(atividade);
	    atividade.getCalendarios().remove(this);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Evento> getEventos() {
		return eventos;
	}

	public void setEventos(List<Evento> eventos) {
		this.eventos = eventos;
	}

	public Aluno getAluno() {
		return aluno;
	}

	public void setAluno(Aluno aluno) {
		this.aluno = aluno;
	}
	
	
}
