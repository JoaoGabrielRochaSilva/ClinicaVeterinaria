package com.example.trabalho.clinicaveterinaria.agendamento.dto;

import java.time.LocalDateTime;

import com.example.trabalho.clinicaveterinaria.common.enums.StatusAgendamento;

public record AgendamentoResponse(
    Long id,
    LocalDateTime dataHora,
    String motivo,
    StatusAgendamento statusAgendamento,
    Long idAnimal,
    String nomeAnimal,
    Long idVeterinario,
    String nomeVeterinario
) {
}