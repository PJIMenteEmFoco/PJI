package br.edu.ifsp.mef.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Dados permitidos na criação de um evento do calendário psicopedagógico. */
public record CriarEventoPsicopedagogoRequest(
        String titulo,
        String descricao,
        LocalDate dataEvento,
        LocalTime horario,
        PrioridadeEvento prioridade) {
}
