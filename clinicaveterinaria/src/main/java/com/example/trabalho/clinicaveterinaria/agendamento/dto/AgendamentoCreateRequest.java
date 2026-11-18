package com.example.trabalho.clinicaveterinaria.agendamento.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AgendamentoCreateRequest(
    @NotNull (message = "Animal é obrigatório")
    Long idAnimal,
    String nomeAnimal,
    @NotNull (message = "Veterinário é obrigatório")
    Long idVeterinario,
    String nomeVeterinario,
    @NotNull (message = "Data é obrigatória")
    LocalDate data,
    @NotNull (message = "Hora é obrigatória")
    LocalTime hora,
    @NotBlank (message = "Motivo é obrigatório")
    @Size (max = 255, message = "Motivo deve ter no máximo 255 caracteres")
    String motivo
) {
}