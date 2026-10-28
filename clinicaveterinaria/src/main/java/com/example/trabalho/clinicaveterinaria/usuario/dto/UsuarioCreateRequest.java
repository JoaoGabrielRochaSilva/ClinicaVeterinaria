package com.example.trabalho.clinicaveterinaria.usuario.dto;

import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioCreateRequest(
    @NotBlank (message = "Nome é obrigatório")
    String nome,
    @NotBlank (message = "Email é obrigatório")
    @Email (message = "Email invalido")
    String email,
    @NotBlank (message = "Senha é obrigatória")
    String senha,
    @NotBlank (message = "Permissão é obrigatória")
    Permissao permissao) {

}
