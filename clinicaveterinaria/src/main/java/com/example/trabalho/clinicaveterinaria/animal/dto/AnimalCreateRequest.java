package com.example.trabalho.clinicaveterinaria.animal.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public record AnimalCreateRequest(
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
    @Min (value = 1, message = "Informe um peso válido")
    Double peso,
    @NotNull (message = "Dono é obrigatório")
    Long idDono,
    String nomeDono
) {
}
