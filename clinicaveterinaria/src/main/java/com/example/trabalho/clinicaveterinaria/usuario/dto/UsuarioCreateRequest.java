package com.example.trabalho.clinicaveterinaria.usuario.dto;

import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioCreateRequest(
    @NotBlank (message = "Nome é obrigatório")
    String nome,
    @Email (message = "O e-mail informado não possui um formato válido")
    @NotBlank (message = "Email é obrigatório")
    String email,
    @NotBlank (message = "Senha é obrigatória")
    String senha,
    @NotNull (message = "Permissão é obrigatória")
    Permissao permissao) {

}
