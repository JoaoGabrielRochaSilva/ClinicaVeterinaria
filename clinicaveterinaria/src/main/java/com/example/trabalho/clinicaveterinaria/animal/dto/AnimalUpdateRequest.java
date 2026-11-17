package com.example.trabalho.clinicaveterinaria.animal.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

public record AnimalUpdateRequest(
    @NotBlank (message = "Nome é obrigatório")
    String nome,
    @NotBlank (message = "Espécie é obrigatória")
    String especie,
    @NotNull (message = "Data de nascimento é obrigatória")
    @PastOrPresent (message = "Data de nascimento deve estar no passado ou presente")
    LocalDate dataNascimento,
    @NotBlank (message = "Sexo do animal é obrigatório")
    String sexo,
    @NotNull (message = "Peso do animal é obrigatório")
    @Positive (message = "Informe um peso válido")
    Double peso
) {

}
