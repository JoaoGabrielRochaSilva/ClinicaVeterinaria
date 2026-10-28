package com.example.trabalho.clinicaveterinaria.prontuario;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service 
@AllArgsConstructor 
public class ProntuarioService {
    private final ProntuarioRepository prontuarioRepository;
}
