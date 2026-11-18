package com.example.trabalho.clinicaveterinaria.agendamento.dto;

import java.time.LocalDate;

import com.example.trabalho.clinicaveterinaria.common.enums.StatusAgendamento;

public record AgendamentoSearchRequest(
    Long idAnimal,
    String nomeAnimal,
    Long idVeterinario,
    String nomeVeterinario,
    LocalDate dataInicio,
    LocalDate dataFim,
    StatusAgendamento statusAgendamento
) {
}