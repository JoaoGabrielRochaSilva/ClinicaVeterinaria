package com.example.trabalho.clinicaveterinaria.prontuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.trabalho.clinicaveterinaria.common.enums.Status;

public interface ProntuarioRepository extends JpaRepository<Prontuario, Long>, JpaSpecificationExecutor<Prontuario> {

    Optional<Prontuario> findByIdAndStatusAndAnimalStatus(Long id, Status status, Status statusAnimal);

    Optional<Prontuario> findByAnimalIdAndStatusAndAnimalStatus(Long idAnimal, Status status, Status statusAnimal);
}