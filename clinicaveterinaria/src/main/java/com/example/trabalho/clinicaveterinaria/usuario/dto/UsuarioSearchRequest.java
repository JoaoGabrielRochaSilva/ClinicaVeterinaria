package com.example.trabalho.clinicaveterinaria.usuario.dto;

import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;

public record UsuarioSearchRequest(Long id, String nome, String email, Permissao permissao) {
    public boolean isEmpty() {
        return id == null && 
               (nome == null || nome.isBlank()) &&
               (email == null || email.isBlank()) &&
               (permissao == null);
    }
}
