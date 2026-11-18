package com.example.trabalho.clinicaveterinaria.agendamento.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record AgendamentoRemarcarRequest(
    @NotNull (message = "Data é obrigatória")
    LocalDate data,
    @NotNull (message = "Hora é obrigatória")
    LocalTime hora
) {
}