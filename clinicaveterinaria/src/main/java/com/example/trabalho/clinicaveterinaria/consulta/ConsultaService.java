package com.example.trabalho.clinicaveterinaria.consulta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.trabalho.clinicaveterinaria.agendamento.Agendamento;
import com.example.trabalho.clinicaveterinaria.agendamento.AgendamentoService;
import com.example.trabalho.clinicaveterinaria.common.enums.Status;
import com.example.trabalho.clinicaveterinaria.consulta.dto.ConsultaCreateRequest;
import com.example.trabalho.clinicaveterinaria.consulta.dto.ConsultaResponse;
import com.example.trabalho.clinicaveterinaria.consulta.dto.ConsultaSearchRequest;
import com.example.trabalho.clinicaveterinaria.consulta.dto.ConsultaUpdateRequest;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final AgendamentoService agendamentoService;

    private ConsultaResponse toResponse(Consulta consulta) {
        // O animal e o veterinário vêm do agendamento
        Agendamento agendamento = consulta.getAgendamento();

        return new ConsultaResponse(
            consulta.getId(),
            consulta.getDataHora(),
            consulta.getDiagnostico(),
            consulta.getTratamento(),
            agendamento.getId(),
            agendamento.getAnimal().getId(),
            agendamento.getAnimal().getNome(),
            agendamento.getVeterinario().getId(),
            agendamento.getVeterinario().getNome()
        );
    }

    // Consulta excluída logicamente (INATIVO) é tratada como inexistente
    private Consulta buscarAtiva(Long id) {
        return consultaRepository.findByIdAndStatus(id, Status.ATIVO)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Consulta não encontrada"
            ));
    }

    // Registra o atendimento. Tudo em UMA transação, se qualquer passo falhar
    // o agendamento volta a PENDENTE e nenhuma consulta é gravada.
    @Transactional
    public ConsultaResponse registrar(ConsultaCreateRequest request) {
        // Valida (existe, ativo, PENDENTE) e passa para REALIZADO
        Agendamento agendamento = agendamentoService.marcarRealizado(request.idAgendamento());

        Consulta consulta = new Consulta();
        consulta.setDataHora(LocalDateTime.now());
        consulta.setDiagnostico(request.diagnostico().trim());
        consulta.setTratamento(request.tratamento().trim());
        consulta.setAgendamento(agendamento);

        return toResponse(consultaRepository.save(consulta));
    }

    @Transactional(readOnly = true)
    public Page<ConsultaResponse> pesquisar(
            ConsultaSearchRequest request,
            Pageable pageable) {

        Specification<Consulta> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), Status.ATIVO));

            if (request.idAnimal() != null) {
                predicates.add(
                    cb.equal(
                        root.get("agendamento").get("animal").get("id"),
                        request.idAnimal()
                    )
                );
            }

            if (request.idVeterinario() != null) {
                predicates.add(
                    cb.equal(
                        root.get("agendamento").get("veterinario").get("id"),
                        request.idVeterinario()
                    )
                );
            }

            if (request.dataInicio() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("dataHora"),
                        request.dataInicio().atStartOfDay()
                    )
                );
            }

            if (request.dataFim() != null) {
                predicates.add(
                    cb.lessThan(
                        root.get("dataHora"),
                        request.dataFim().plusDays(1).atStartOfDay()
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return consultaRepository.findAll(specification, pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ConsultaResponse buscarPorId(Long id) {
        return toResponse(buscarAtiva(id));
    }

    // Só diagnóstico e tratamento mudam, data/hora e agendamento são preservados
    @Transactional
    public void atualizar(Long id, ConsultaUpdateRequest request) {
        Consulta consulta = buscarAtiva(id);

        consulta.setDiagnostico(request.diagnostico().trim());
        consulta.setTratamento(request.tratamento().trim());

        consultaRepository.save(consulta);
    }

    // Exclusão lógica, o registro é preservado
    @Transactional
    public void excluir(Long id) {
        Consulta consulta = buscarAtiva(id);

        consulta.setStatus(Status.INATIVO);
        consultaRepository.save(consulta);
    }
}