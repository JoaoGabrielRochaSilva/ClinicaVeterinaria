package com.example.trabalho.clinicaveterinaria.animal.dto;

import java.time.LocalDate;

public record AnimalSearchRequest(Long id, String nome, String especie, LocalDate dataNascimentoInicio, LocalDate dataNascimentoFim,
    String sexo, Double peso, Long idDono, String nomeDono
) {

}
