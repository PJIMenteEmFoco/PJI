package br.edu.ifsp.mef.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
public class Professor extends Usuario {
	@OneToMany(mappedBy = "professorResponsavel")
	private List<Turma> turmasProfessor = new ArrayList<>();
	@OneToMany(mappedBy = "professor")
	private List<Atividade> atividades = new ArrayList<>();

	
	
	public List<Turma> getTurmasProfessor() {
		return turmasProfessor;
	}

	public void setTurmas(List<Turma> turmasProfessor) {
		this.turmasProfessor = turmasProfessor;
	}

	public List<Atividade> getAtividades() {
		return atividades;
	}

	public void setAtividades(List<Atividade> atividades) {
		this.atividades = atividades;
	}

	
	
	
}
