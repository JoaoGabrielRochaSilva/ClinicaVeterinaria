package com.example.trabalho.clinicaveterinaria.agendamento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.trabalho.clinicaveterinaria.common.enums.Status;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>, JpaSpecificationExecutor<Agendamento> {

  Optional<Agendamento> findByIdAndStatus(Long id, Status status);

  @Query("""
      select a.id from Agendamento a
      where a.animal.id = :idAnimal
        and a.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.ATIVO
        and a.statusAgendamento = com.example.trabalho.clinicaveterinaria.common.enums.StatusAgendamento.PENDENTE
        and a.dataHora > :inicio
        and a.dataHora < :fim
      """)
  List<Long> buscarIdsPendentesDoAnimal(
      @Param("idAnimal") Long idAnimal,
      @Param("inicio") LocalDateTime inicio,
      @Param("fim") LocalDateTime fim);

  @Query("""
      select a.id from Agendamento a
      where a.veterinario.id = :idVeterinario
        and a.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.ATIVO
        and a.statusAgendamento = com.example.trabalho.clinicaveterinaria.common.enums.StatusAgendamento.PENDENTE
        and a.dataHora > :inicio
        and a.dataHora < :fim
      """)
  List<Long> buscarIdsPendentesDoVeterinario(
      @Param("idVeterinario") Long idVeterinario,
      @Param("inicio") LocalDateTime inicio,
      @Param("fim") LocalDateTime fim);

  @Modifying
  @Query("""
      update Consulta c
      set c.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.INATIVO
      where c.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.ATIVO
        and c.agendamento.id in (select a.id from Agendamento a where a.animal.id = :idAnimal)
      """)
  int inativarConsultasDoAnimal(@Param("idAnimal") Long idAnimal);

  @Modifying
  @Query("""
      update Agendamento a
      set a.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.INATIVO
      where a.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.ATIVO
        and a.animal.id = :idAnimal
      """)
  int inativarAgendamentosDoAnimal(@Param("idAnimal") Long idAnimal);

  @Modifying
  @Query("""
      update Consulta c
      set c.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.INATIVO
      where c.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.ATIVO
        and c.agendamento.id in (select a.id from Agendamento a where a.veterinario.id = :idVeterinario)
      """)
  int inativarConsultasDoVeterinario(@Param("idVeterinario") Long idVeterinario);

  @Modifying
  @Query("""
      update Agendamento a
      set a.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.INATIVO
      where a.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.ATIVO
        and a.veterinario.id = :idVeterinario
      """)
  int inativarAgendamentosDoVeterinario(@Param("idVeterinario") Long idVeterinario);

  @Modifying
  @Query("""
      update Consulta c
      set c.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.INATIVO
      where c.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.ATIVO
        and c.agendamento.id in (
          select a.id from Agendamento a
          where a.animal.id in (select an.id from Animal an where an.dono.id = :idDono))
      """)
  int inativarConsultasDoCliente(@Param("idDono") Long idDono);

  @Modifying
  @Query("""
      update Agendamento a
      set a.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.INATIVO
      where a.status = com.example.trabalho.clinicaveterinaria.common.enums.Status.ATIVO
        and a.animal.id in (select an.id from Animal an where an.dono.id = :idDono)
      """)
  int inativarAgendamentosDoCliente(@Param("idDono") Long idDono);

  default void inativarDoCliente(Long idDono) {
    inativarConsultasDoCliente(idDono);
    inativarAgendamentosDoCliente(idDono);
  }

  default void inativarDoAnimal(Long idAnimal) {
    inativarConsultasDoAnimal(idAnimal);
    inativarAgendamentosDoAnimal(idAnimal);
  }

  default void inativarDoVeterinario(Long idVeterinario) {
    inativarConsultasDoVeterinario(idVeterinario);
    inativarAgendamentosDoVeterinario(idVeterinario);
  }
}