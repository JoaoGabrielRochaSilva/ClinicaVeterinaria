package com.example.trabalho.clinicaveterinaria.prontuario;

import org.springframework.stereotype.Controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Controller 
@AllArgsConstructor 
public class ProntuarioController {
    private final ProntuarioService prontuarioService;
}
