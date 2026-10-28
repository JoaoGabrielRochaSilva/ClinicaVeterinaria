package com.example.trabalho.clinicaveterinaria.usuario;

import org.springframework.stereotype.Controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Controller
@AllArgsConstructor 
public class UsuarioController {
    private final UsuarioService usuarioService;
}
