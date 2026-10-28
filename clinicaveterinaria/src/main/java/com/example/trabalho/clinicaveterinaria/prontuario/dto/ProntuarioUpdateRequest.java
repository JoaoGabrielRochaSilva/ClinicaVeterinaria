package com.example.trabalho.clinicaveterinaria.prontuario.dto;

public record ProntuarioUpdateRequest(
    String alergias,
    String doencasPreExistentes,
    String observacoes
) {

}
