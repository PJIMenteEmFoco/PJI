package br.edu.ifsp.mef.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Pergunta {
	
	@Id
	@GeneratedValue
	private Long id;
	@ManyToOne
	@JoinColumn(name = "idQuiz")
	private Quiz quiz;
	private String enunciado;
	private String alternativa1;
	private String alternativa2;
	private String alternativa3;
	private String alternativa4;
	private String alternativa5;
	private int alternativaCorreta;
	
	
	public Pergunta() {}
	
	public Pergunta(int alternativaCorreta) {
		this.alternativaCorreta = alternativaCorreta;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Quiz getQuiz() {
		return quiz;
	}
	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}
	
	public String getEnunciado() {
		return enunciado;
	}

	public void setEnunciado(String enunciado) {
		this.enunciado = enunciado;
	}

	public String getAlternativa1() {
		return alternativa1;
	}
	public void setAlternativa1(String alternativa1) {
		this.alternativa1 = alternativa1;
	}
	public String getAlternativa2() {
		return alternativa2;
	}
	public void setAlternativa2(String alternativa2) {
		this.alternativa2 = alternativa2;
	}
	public String getAlternativa3() {
		return alternativa3;
	}
	public void setAlternativa3(String alternativa3) {
		this.alternativa3 = alternativa3;
	}
	public String getAlternativa4() {
		return alternativa4;
	}
	public void setAlternativa4(String alternativa4) {
		this.alternativa4 = alternativa4;
	}
	public String getAlternativa5() {
		return alternativa5;
	}
	public void setAlternativa5(String alternativa5) {
		this.alternativa5 = alternativa5;
	}
	public int getAlternativaCorreta() {
		return alternativaCorreta;
	}
	public void setAlternativaCorreta(int alternativaCorreta) {
		this.alternativaCorreta = alternativaCorreta;
	}
	
	
	
}
