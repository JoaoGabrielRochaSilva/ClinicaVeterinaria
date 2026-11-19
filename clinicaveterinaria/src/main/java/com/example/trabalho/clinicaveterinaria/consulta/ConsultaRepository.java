package com.example.trabalho.clinicaveterinaria.consulta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.trabalho.clinicaveterinaria.common.enums.Status;

public interface ConsultaRepository extends JpaRepository<Consulta, Long>, JpaSpecificationExecutor<Consulta> {
    Optional<Consulta> findByIdAndStatus(Long id, Status status);
}