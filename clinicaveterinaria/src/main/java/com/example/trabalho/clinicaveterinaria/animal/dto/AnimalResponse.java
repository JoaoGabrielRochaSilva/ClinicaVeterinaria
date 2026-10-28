package com.example.trabalho.clinicaveterinaria.animal.dto;

import java.time.LocalDate;

public record AnimalResponse(Long id, String nome, String especie, LocalDate dataNascimento,
    String sexo, Double peso, Long idDono, String nomeDone

) {

}
