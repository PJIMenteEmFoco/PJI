package br.edu.ifsp.mef.model;

import java.time.LocalDate;

public record FeriadoApiDTO(
    LocalDate date,
    String name,
    String type
) {
}
