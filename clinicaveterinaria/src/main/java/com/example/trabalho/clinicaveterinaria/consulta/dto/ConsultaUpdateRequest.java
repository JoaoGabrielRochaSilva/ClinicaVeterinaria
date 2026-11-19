package com.example.trabalho.clinicaveterinaria.consulta.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsultaUpdateRequest(
    @NotBlank (message = "Diagnóstico é obrigatório")
    String diagnostico,
    @NotBlank (message = "Tratamento é obrigatório")
    String tratamento
) {
}