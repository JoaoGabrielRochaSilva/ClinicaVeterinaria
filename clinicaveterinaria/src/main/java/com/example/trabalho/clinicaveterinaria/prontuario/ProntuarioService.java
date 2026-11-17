package com.example.trabalho.clinicaveterinaria.prontuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProntuarioService {
    private final ProntuarioRepository prontuarioRepository;

    @Transactional
    public Prontuario criar() {
        return prontuarioRepository.save(new Prontuario());
    }
}
