package com.example.trabalho.clinicaveterinaria.animal;

import org.springframework.stereotype.Controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller 
@AllArgsConstructor
public class AnimalController {
    private final AnimalService animalService;
}
