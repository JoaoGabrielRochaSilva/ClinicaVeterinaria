package com.example.trabalho.clinicaveterinaria.animal;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.trabalho.clinicaveterinaria.common.enums.Status;

public interface AnimalRepository
                extends JpaRepository<Animal, Long>,
                JpaSpecificationExecutor<Animal> {

        Optional<Animal> findByIdAndStatus(Long id, Status status);

        List<Animal> findTop10ByNomeContainingIgnoreCaseAndStatusOrderByNomeAsc(String nome, Status status);

        @Modifying
        @Query("""
                update Animal a
                set a.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.INATIVO
                where a.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.ATIVO
                and a.dono.id = :idDono
                """)
        int inativarDoDono(@Param("idDono") Long idDono);
}