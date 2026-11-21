package com.example.trabalho.clinicaveterinaria.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioUpdateRequest(
    @NotBlank (message = "Nome é obrigatório")
    String nome,
    @Email (message = "O e-mail informado não possui um formato válido")
    @NotBlank (message = "Email é obrigatório")
    String email) {

}