package br.edu.ifsp.mef.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class LeituraGuiada {

	@Id
	private Long id;
	@OneToOne
	@MapsId
	@JoinColumn(name = "idAtividade")
	private Atividade atividade;

	private String arquivoPdf;            
	private int perguntasPorTrecho;
	private String nivelEscolar;          

	@Enumerated(EnumType.STRING)
	private StatusLeitura status = StatusLeitura.PROCESSANDO;

	private String mensagemErro;

	@OneToMany(mappedBy = "leitura", cascade = CascadeType.ALL)
	private List<TrechoLeitura> trechos = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Atividade getAtividade() {
		return atividade;
	}

	public void setAtividade(Atividade atividade) {
		this.atividade = atividade;
	}

	public String getArquivoPdf() {
		return arquivoPdf;
	}

	public void setArquivoPdf(String arquivoPdf) {
		this.arquivoPdf = arquivoPdf;
	}

	public int getPerguntasPorTrecho() {
		return perguntasPorTrecho;
	}

	public void setPerguntasPorTrecho(int perguntasPorTrecho) {
		this.perguntasPorTrecho = perguntasPorTrecho;
	}

	public String getNivelEscolar() {
		return nivelEscolar;
	}

	public void setNivelEscolar(String nivelEscolar) {
		this.nivelEscolar = nivelEscolar;
	}

	public StatusLeitura getStatus() {
		return status;
	}

	public void setStatus(StatusLeitura status) {
		this.status = status;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	public List<TrechoLeitura> getTrechos() {
		return trechos;
	}

	public void setTrechos(List<TrechoLeitura> trechos) {
		this.trechos = trechos;
	}

	public void addTrecho(TrechoLeitura trecho) {
		trechos.add(trecho);
		trecho.setLeitura(this);
	}

	public boolean estaAtribuidaAo(Long alunoId) {
		return atividade.getAlunos().stream().anyMatch(a -> a.getId().equals(alunoId));
	}
}

