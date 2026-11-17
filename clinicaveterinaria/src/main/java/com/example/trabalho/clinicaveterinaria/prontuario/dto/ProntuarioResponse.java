package com.example.trabalho.clinicaveterinaria.prontuario.dto;

public record ProntuarioResponse(
    Long id,
    Long idAnimal,
    String nomeAnimal,
    String alergias,
    String doencasPreExistentes,
    String observacoes
) {

}