package com.example.trabalho.clinicaveterinaria.usuario;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor 
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
}
