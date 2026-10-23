package br.edu.ifsp.mef.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

/** Resultado POR aluno, sem alterar o status global de Atividade. */
@Entity
public class TentativaAtividade {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_atividade", nullable = false)
    private Atividade atividade;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aluno", nullable = false)
    private Aluno aluno;
    private int acertos;
    private int total;
    private int segundos;
    @Column(nullable = false)
    private LocalDateTime finalizadaEm = LocalDateTime.now();
    public Long getId() { return id; }
    public Atividade getAtividade() { return atividade; }
    public void setAtividade(Atividade a) { atividade = a; }
    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno a) { aluno = a; }
    public int getAcertos() { return acertos; }
    public void setAcertos(int a) { acertos = a; }
    public int getTotal() { return total; }
    public void setTotal(int t) { total = t; }
    public int getSegundos() { return segundos; }
    public void setSegundos(int s) { segundos = s; }
    public LocalDateTime getFinalizadaEm() { return finalizadaEm; }
}
