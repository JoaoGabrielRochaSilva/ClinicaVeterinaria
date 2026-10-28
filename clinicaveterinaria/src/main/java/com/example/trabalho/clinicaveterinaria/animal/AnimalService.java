package com.example.trabalho.clinicaveterinaria.animal;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service 
@AllArgsConstructor 
public class AnimalService {
    private final AnimalRepository animalRepository;
}
