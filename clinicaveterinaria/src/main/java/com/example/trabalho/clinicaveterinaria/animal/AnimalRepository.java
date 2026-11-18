package com.example.trabalho.clinicaveterinaria.animal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.trabalho.clinicaveterinaria.common.enums.Status;

public interface AnimalRepository
        extends JpaRepository<Animal, Long>,
                JpaSpecificationExecutor<Animal> {

    Optional<Animal> findByIdAndStatus(Long id, Status status);

    List<Animal> findTop10ByNomeContainingIgnoreCaseAndStatusOrderByNomeAsc(String nome, Status status);
}