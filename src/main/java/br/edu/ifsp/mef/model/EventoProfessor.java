package br.edu.ifsp.mef.model;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Compromisso que o próprio professor cria no calendário (aula, reunião, aplicação de prova...). */
@Entity
public class EventoProfessor {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "idProfessor")
	private Professor professor;
	private String titulo;
	private String descricao;
	private LocalDate dataEvento;
	private LocalTime horario;   
	@Enumerated(EnumType.STRING)
	private PrioridadeEvento prioridade = PrioridadeEvento.MEDIA;

	public Long getId() { return id; }

	public Professor getProfessor() { return professor; }
	public void setProfessor(Professor professor) { this.professor = professor; }

	public String getTitulo() { return titulo; }
	public void setTitulo(String titulo) { this.titulo = titulo; }

	public String getDescricao() { return descricao; }
	public void setDescricao(String descricao) { this.descricao = descricao; }

	public LocalDate getDataEvento() { return dataEvento; }
	public void setDataEvento(LocalDate dataEvento) { this.dataEvento = dataEvento; }

	public LocalTime getHorario() { return horario; }
	public void setHorario(LocalTime horario) { this.horario = horario; }

	public PrioridadeEvento getPrioridade() { return prioridade; }
	public void setPrioridade(PrioridadeEvento prioridade) { this.prioridade = prioridade; }
}

