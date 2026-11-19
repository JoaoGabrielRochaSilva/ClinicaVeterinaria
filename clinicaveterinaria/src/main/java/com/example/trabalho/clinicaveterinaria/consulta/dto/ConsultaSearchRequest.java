package com.example.trabalho.clinicaveterinaria.consulta.dto;

import java.time.LocalDate;

public record ConsultaSearchRequest(
    Long idAnimal,
    String nomeAnimal,
    Long idVeterinario,
    String nomeVeterinario,
    LocalDate dataInicio,
    LocalDate dataFim
) {
}