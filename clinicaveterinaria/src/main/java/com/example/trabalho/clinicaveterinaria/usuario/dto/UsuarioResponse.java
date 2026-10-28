package com.example.trabalho.clinicaveterinaria.usuario.dto;

import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;

public record UsuarioResponse(Long id, String nome, String email, Permissao permissao) {

}
