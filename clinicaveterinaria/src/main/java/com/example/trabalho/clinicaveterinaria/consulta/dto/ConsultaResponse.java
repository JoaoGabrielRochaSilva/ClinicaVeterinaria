package com.example.trabalho.clinicaveterinaria.consulta.dto;

import java.time.LocalDateTime;

public record ConsultaResponse(
    Long id,
    LocalDateTime dataHora,
    String diagnostico,
    String tratamento,
    Long idAgendamento,
    Long idAnimal,
    String nomeAnimal,
    Long idVeterinario,
    String nomeVeterinario
) {
}