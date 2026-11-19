package com.example.trabalho.clinicaveterinaria.agendamento;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.trabalho.clinicaveterinaria.agendamento.dto.AgendamentoCreateRequest;
import com.example.trabalho.clinicaveterinaria.agendamento.dto.AgendamentoRemarcarRequest;
import com.example.trabalho.clinicaveterinaria.agendamento.dto.AgendamentoResponse;
import com.example.trabalho.clinicaveterinaria.agendamento.dto.AgendamentoSearchRequest;
import com.example.trabalho.clinicaveterinaria.animal.Animal;
import com.example.trabalho.clinicaveterinaria.animal.AnimalService;
import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;
import com.example.trabalho.clinicaveterinaria.common.enums.Status;
import com.example.trabalho.clinicaveterinaria.common.enums.StatusAgendamento;
import com.example.trabalho.clinicaveterinaria.common.exception.RegraDeNegocioException;
import com.example.trabalho.clinicaveterinaria.usuario.Usuario;
import com.example.trabalho.clinicaveterinaria.usuario.UsuarioService;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AgendamentoService {

    private static final LocalTime ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime ULTIMO_INICIO = LocalTime.of(18, 0);
    private static final long DURACAO_MINUTOS = 30;

    private static final String SO_PENDENTE_CANCELAR = "Somente agendamentos pendentes podem ser cancelados";
    private static final String SO_PENDENTE_REMARCAR = "Somente agendamentos pendentes podem ser remarcados";
    private static final String SO_PENDENTE_ATENDER = "Somente agendamentos pendentes podem ser atendidos";

    private final AgendamentoRepository agendamentoRepository;
    private final AnimalService animalService;
    private final UsuarioService usuarioService;

    private AgendamentoResponse toResponse(Agendamento agendamento) {
        return new AgendamentoResponse(
            agendamento.getId(),
            agendamento.getDataHora(),
            agendamento.getMotivo(),
            agendamento.getStatusAgendamento(),
            agendamento.getAnimal().getId(),
            agendamento.getAnimal().getNome(),
            agendamento.getVeterinario().getId(),
            agendamento.getVeterinario().getNome()
        );
    }

    private Agendamento buscarAtivo(Long id) {
        return agendamentoRepository.findByIdAndStatus(id, Status.ATIVO)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Agendamento não encontrado"
            ));
    }

    private void exigirPendente(Agendamento agendamento, String mensagem) {
        if (agendamento.getStatusAgendamento() != StatusAgendamento.PENDENTE) {
            throw new RegraDeNegocioException(mensagem);
        }
    }

    // Valida data e hora e devolve o LocalDateTime (com minutos, sem segundos)
    private LocalDateTime validarHorario(LocalDate data, LocalTime hora) {
        LocalTime inicio = hora.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime dataHora = LocalDateTime.of(data, inicio);
        LocalDateTime agora = LocalDateTime.now();

        if (!dataHora.isAfter(agora)) {
            if (data.isBefore(agora.toLocalDate())) {
                throw new RegraDeNegocioException("data", "A data não pode estar no passado");
            }
            throw new RegraDeNegocioException("hora", "O horário informado já passou");
        }

        DayOfWeek dia = data.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            throw new RegraDeNegocioException("data", "A clínica atende apenas de segunda a sexta-feira");
        }

        if (inicio.isBefore(ABERTURA) || inicio.isAfter(ULTIMO_INICIO)) {
            throw new RegraDeNegocioException("hora", "O horário de início deve ser entre 08:00 e 18:00");
        }

        return dataHora;
    }

    // idIgnorado: usado na remarcação, para o agendamento não conflitar consigo mesmo
    private void validarDisponibilidade(
            Long idAnimal,
            Long idVeterinario,
            LocalDateTime dataHora,
            Long idIgnorado) {

        LocalDateTime inicio = dataHora.minusMinutes(DURACAO_MINUTOS);
        LocalDateTime fim = dataHora.plusMinutes(DURACAO_MINUTOS);

        if (existeOutro(agendamentoRepository.buscarIdsPendentesDoAnimal(idAnimal, inicio, fim), idIgnorado)) {
            throw new RegraDeNegocioException("hora", "O animal já possui um agendamento pendente nesse horário");
        }

        if (existeOutro(agendamentoRepository.buscarIdsPendentesDoVeterinario(idVeterinario, inicio, fim), idIgnorado)) {
            throw new RegraDeNegocioException("hora", "O veterinário já possui um agendamento pendente nesse horário");
        }
    }

    private boolean existeOutro(List<Long> ids, Long idIgnorado) {
        return ids.stream().anyMatch(id -> !id.equals(idIgnorado));
    }

    @Transactional
    public AgendamentoResponse criar(AgendamentoCreateRequest request) {
        LocalDateTime dataHora = validarHorario(request.data(), request.hora());

        Animal animal = animalService.buscarAtivo(request.idAnimal());
        Usuario veterinario = usuarioService.buscarAtivoComPermissao(
            request.idVeterinario(), Permissao.VETERINARIO);

        validarDisponibilidade(animal.getId(), veterinario.getId(), dataHora, null);

        Agendamento agendamento = new Agendamento();
        agendamento.setDataHora(dataHora);
        agendamento.setMotivo(request.motivo().trim());
        agendamento.setAnimal(animal);
        agendamento.setVeterinario(veterinario);
        // statusAgendamento nasce PENDENTE e status nasce ATIVO (padrões da entidade)

        return toResponse(agendamentoRepository.save(agendamento));
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoResponse> pesquisar(
            AgendamentoSearchRequest request,
            Pageable pageable) {

        Specification<Agendamento> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Somente agendamentos ativos (os inativados em cascata ficam ocultos)
            predicates.add(cb.equal(root.get("status"), Status.ATIVO));

            if (request.idAnimal() != null) {
                predicates.add(cb.equal(root.get("animal").get("id"), request.idAnimal()));
            }

            if (request.idVeterinario() != null) {
                predicates.add(cb.equal(root.get("veterinario").get("id"), request.idVeterinario()));
            }

            if (request.statusAgendamento() != null) {
                predicates.add(cb.equal(root.get("statusAgendamento"), request.statusAgendamento()));
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

        return agendamentoRepository.findAll(specification, pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse buscarPorId(Long id) {
        return toResponse(buscarAtivo(id));
    }

    // Usado para abrir a tela de remarcação: só PENDENTE pode ser remarcado
    @Transactional(readOnly = true)
    public AgendamentoResponse buscarParaRemarcacao(Long id) {
        Agendamento agendamento = buscarAtivo(id);
        exigirPendente(agendamento, SO_PENDENTE_REMARCAR);
        return toResponse(agendamento);
    }

    // Usado pela tela de atendimento, só PENDENTE pode ser atendido
    @Transactional(readOnly = true)
    public AgendamentoResponse buscarParaAtendimento(Long id) {
        Agendamento agendamento = buscarAtivo(id);
        exigirPendente(agendamento, SO_PENDENTE_ATENDER);
        return toResponse(agendamento);
    }

    // Passa o agendamento para REALIZADO. Chamado pelo ConsultaService dentro da
    // transação do atendimento, se algo falhar depois, o status também é desfeito.
    @Transactional
    public Agendamento marcarRealizado(Long id) {
        Agendamento agendamento = buscarAtivo(id);
        exigirPendente(agendamento, SO_PENDENTE_ATENDER);

        agendamento.setStatusAgendamento(StatusAgendamento.REALIZADO);
        return agendamentoRepository.save(agendamento);
    }

    // Altera somente data/hora do MESMO registro (o id é preservado)
    @Transactional
    public void remarcar(Long id, AgendamentoRemarcarRequest request) {
        Agendamento agendamento = buscarAtivo(id);
        exigirPendente(agendamento, SO_PENDENTE_REMARCAR);

        LocalDateTime novaDataHora = validarHorario(request.data(), request.hora());

        validarDisponibilidade(
            agendamento.getAnimal().getId(),
            agendamento.getVeterinario().getId(),
            novaDataHora,
            agendamento.getId());

        agendamento.setDataHora(novaDataHora);
        agendamentoRepository.save(agendamento);
    }

    @Transactional
    public void cancelar(Long id) {
        Agendamento agendamento = buscarAtivo(id);
        exigirPendente(agendamento, SO_PENDENTE_CANCELAR);

        agendamento.setStatusAgendamento(StatusAgendamento.CANCELADO);
        agendamentoRepository.save(agendamento);
    }
}