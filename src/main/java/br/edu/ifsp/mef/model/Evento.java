package br.edu.ifsp.mef.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

@Entity
public class Evento {
    @Id
    @GeneratedValue
    private Long id;

    private String nome;
    private String descricao;
    private LocalDate dataInicio;
    private LocalDate dataFim;

    @Column(name = "horario")
    private LocalTime horario;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridade")
    private PrioridadeEvento prioridade = PrioridadeEvento.MEDIA;

    @ManyToOne
    @JoinColumn(name = "idCalendario")
    private Calendario calendario;

    @PrePersist
    void definirPrioridadePadrao() {
        if (prioridade == null) {
            prioridade = PrioridadeEvento.MEDIA;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }
    public PrioridadeEvento getPrioridade() { return prioridade; }
    public void setPrioridade(PrioridadeEvento prioridade) { this.prioridade = prioridade; }
    public Calendario getCalendario() { return calendario; }
    public void setCalendario(Calendario calendario) { this.calendario = calendario; }
}
