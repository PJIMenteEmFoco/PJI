package br.edu.ifsp.mef.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Compromisso criado pelo próprio psicopedagogo no calendário. */
@Entity
@Table(name = "evento_psicopedagogo")
public class EventoPsicopedagogo {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPsicopedagogo", nullable = false)
    private Psicopedagogo psicopedagogo;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 1000)
    private String descricao;

    @Column(name = "data_evento", nullable = false)
    private LocalDate dataEvento;

    private LocalTime horario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PrioridadeEvento prioridade = PrioridadeEvento.MEDIA;

    @PrePersist
    void definirPrioridadePadrao() {
        if (prioridade == null) {
            prioridade = PrioridadeEvento.MEDIA;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Psicopedagogo getPsicopedagogo() { return psicopedagogo; }
    public void setPsicopedagogo(Psicopedagogo psicopedagogo) { this.psicopedagogo = psicopedagogo; }

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
