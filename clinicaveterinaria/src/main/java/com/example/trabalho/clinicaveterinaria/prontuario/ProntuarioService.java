package com.example.trabalho.clinicaveterinaria.prontuario;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.trabalho.clinicaveterinaria.animal.Animal;
import com.example.trabalho.clinicaveterinaria.common.enums.Status;
import com.example.trabalho.clinicaveterinaria.prontuario.dto.ProntuarioResponse;
import com.example.trabalho.clinicaveterinaria.prontuario.dto.ProntuarioSearchRequest;
import com.example.trabalho.clinicaveterinaria.prontuario.dto.ProntuarioUpdateRequest;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;

    // Campo em branco é gravado como null (vazio e "não informado" são a mesma coisa)
    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private ProntuarioResponse toResponse(Prontuario prontuario) {
        Animal animal = prontuario.getAnimal();

        return new ProntuarioResponse(
            prontuario.getId(),
            animal.getId(),
            animal.getNome(),
            prontuario.getAlergias(),
            prontuario.getDoencasPreExistentes(),
            prontuario.getObservacoes()
        );
    }

    // Só enxerga prontuários ativos de animais ativos
    private Prontuario buscarAtivo(Long id) {
        return prontuarioRepository
            .findByIdAndStatusAndAnimalStatus(id, Status.ATIVO, Status.ATIVO)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Prontuário não encontrado"
            ));
    }

    @Transactional
    public Prontuario criar(String alergias, String doencasPreExistentes, String observacoes) {
        Prontuario prontuario = new Prontuario();

        prontuario.setAlergias(normalizar(alergias));
        prontuario.setDoencasPreExistentes(normalizar(doencasPreExistentes));
        prontuario.setObservacoes(normalizar(observacoes));

        return prontuarioRepository.save(prontuario);
    }

    @Transactional(readOnly = true)
    public Page<ProntuarioResponse> pesquisar(
            ProntuarioSearchRequest request,
            Pageable pageable) {

        Specification<Prontuario> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Prontuario, Animal> animal = root.join("animal", JoinType.INNER);

            predicates.add(cb.equal(root.get("status"), Status.ATIVO));
            predicates.add(cb.equal(animal.get("status"), Status.ATIVO));

            if (request.idAnimal() != null) {
                predicates.add(cb.equal(animal.get("id"), request.idAnimal()));
            }

            if (request.nomeAnimal() != null && !request.nomeAnimal().isBlank()) {
                predicates.add(
                    cb.like(
                        cb.lower(animal.get("nome")),
                        "%" + request.nomeAnimal().trim().toLowerCase() + "%"
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return prontuarioRepository.findAll(specification, pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProntuarioResponse buscarPorId(Long id) {
        return toResponse(buscarAtivo(id));
    }

    @Transactional(readOnly = true)
    public ProntuarioResponse buscarPorAnimal(Long idAnimal) {
        Prontuario prontuario = prontuarioRepository
            .findByAnimalIdAndStatusAndAnimalStatus(idAnimal, Status.ATIVO, Status.ATIVO)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Prontuário não encontrado"
            ));

        return toResponse(prontuario);
    }

    // Atualiza apenas os três campos clínicos
    @Transactional
    public void atualizar(Long id, ProntuarioUpdateRequest request) {
        Prontuario prontuario = buscarAtivo(id);

        prontuario.setAlergias(normalizar(request.alergias()));
        prontuario.setDoencasPreExistentes(normalizar(request.doencasPreExistentes()));
        prontuario.setObservacoes(normalizar(request.observacoes()));

        prontuarioRepository.save(prontuario);
    }
}