package br.edu.ifsp.mef.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class TrechoLeitura {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "idLeitura")
	private LeituraGuiada leitura;

	private int ordem;

	private String texto;

	@OneToMany(mappedBy = "trecho", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PerguntaLeitura> perguntas = new ArrayList<>();

	public TrechoLeitura() {
	}

	public TrechoLeitura(int ordem, String texto) {
		this.ordem = ordem;
		this.texto = texto;
	}

	public Long getId() {
		return id;
	}

	public LeituraGuiada getLeitura() {
		return leitura;
	}

	public void setLeitura(LeituraGuiada leitura) {
		this.leitura = leitura;
	}

	public int getOrdem() {
		return ordem;
	}

	public void setOrdem(int ordem) {
		this.ordem = ordem;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public List<PerguntaLeitura> getPerguntas() {
		return perguntas;
	}

	public void addPergunta(PerguntaLeitura pergunta) {
		perguntas.add(pergunta);
		pergunta.setTrecho(this);
	}
}
