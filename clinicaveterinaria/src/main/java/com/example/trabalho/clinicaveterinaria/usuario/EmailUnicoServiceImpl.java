package com.example.trabalho.clinicaveterinaria.usuario;

import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmailUnicoServiceImpl implements EmailUnicoService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public boolean isValueUnique(Object value, String attributeName, Object rootObject) {
        if (value == null) {
            return true;
        }
        return !usuarioRepository.existsByEmail(value.toString());
    }
}