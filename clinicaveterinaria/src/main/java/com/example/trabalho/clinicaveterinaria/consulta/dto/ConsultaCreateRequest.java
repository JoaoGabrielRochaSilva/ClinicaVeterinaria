package com.example.trabalho.clinicaveterinaria.consulta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsultaCreateRequest(
    @NotNull (message = "Agendamento é obrigatório")
    Long idAgendamento,
    @NotBlank (message = "Diagnóstico é obrigatório")
    String diagnostico,
    @NotBlank (message = "Tratamento é obrigatório")
    String tratamento
) {
}