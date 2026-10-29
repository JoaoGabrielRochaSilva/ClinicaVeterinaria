package com.example.trabalho.clinicaveterinaria.animal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.trabalho.clinicaveterinaria.common.enums.Status;

import java.util.Optional;

public interface AnimalRepository
        extends JpaRepository<Animal, Long>,
                JpaSpecificationExecutor<Animal> {

    Optional<Animal> findByIdAndStatus(Long id, Status status);
}