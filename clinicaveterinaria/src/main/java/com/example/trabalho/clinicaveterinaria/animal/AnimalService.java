package com.example.trabalho.clinicaveterinaria.animal;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.trabalho.clinicaveterinaria.animal.dto.AnimalCreateRequest;
import com.example.trabalho.clinicaveterinaria.animal.dto.AnimalResponse;
import com.example.trabalho.clinicaveterinaria.animal.dto.AnimalSearchRequest;
import com.example.trabalho.clinicaveterinaria.animal.dto.AnimalUpdateRequest;
import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;
import com.example.trabalho.clinicaveterinaria.common.enums.Status;
import com.example.trabalho.clinicaveterinaria.prontuario.Prontuario;
import com.example.trabalho.clinicaveterinaria.prontuario.ProntuarioRepository;
import com.example.trabalho.clinicaveterinaria.usuario.Usuario;
import com.example.trabalho.clinicaveterinaria.usuario.UsuarioRepository;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProntuarioRepository prontuarioRepository;

    private AnimalResponse toResponse(Animal animal) {
        return new AnimalResponse(
            animal.getId(),
            animal.getNome(),
            animal.getEspecie(),
            animal.getDataNascimento(),
            animal.getSexo(),
            animal.getPeso(),
            animal.getDono().getId(),
            animal.getDono().getNome()
        );
    }

    private Animal buscarAtivo(Long id) {
        return animalRepository.findByIdAndStatus(id, Status.ATIVO)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Animal não encontrado"
            ));
    }

    @Transactional(readOnly = true)
    public Page<AnimalResponse> pesquisar(
            AnimalSearchRequest request,
            Pageable pageable) {

        Specification<Animal> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Somente animais ativos
            predicates.add(
                cb.equal(root.get("status"), Status.ATIVO)
            );

            if (request.id() != null) {
                predicates.add(
                    cb.equal(root.get("id"), request.id())
                );
            }

            if (request.nome() != null && !request.nome().isBlank()) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("nome")),
                        "%" + request.nome().trim().toLowerCase() + "%"
                    )
                );
            }

            if (request.especie() != null && !request.especie().isBlank()) {
                predicates.add(
                    cb.like(
                        cb.lower(root.get("especie")),
                        "%" + request.especie().trim().toLowerCase() + "%"
                    )
                );
            }

            if (request.dataNascimentoInicio() != null) {
                predicates.add(
                    cb.greaterThanOrEqualTo(
                        root.get("dataNascimento"),
                        request.dataNascimentoInicio()
                    )
                );
            }

            if (request.dataNascimentoFim() != null) {
                predicates.add(
                    cb.lessThanOrEqualTo(
                        root.get("dataNascimento"),
                        request.dataNascimentoFim()
                    )
                );
            }

            if (request.sexo() != null && !request.sexo().isBlank()) {
                predicates.add(
                    cb.equal(
                        cb.lower(root.get("sexo")),
                        request.sexo().trim().toLowerCase()
                    )
                );
            }

            if (request.peso() != null) {
                predicates.add(
                    cb.equal(root.get("peso"), request.peso())
                );
            }

            if (request.idDono() != null) {
                predicates.add(
                    cb.equal(
                        root.join("dono", JoinType.INNER).get("id"),
                        request.idDono()
                    )
                );
            }

            if (request.nomeDono() != null && !request.nomeDono().isBlank()) {
                predicates.add(
                    cb.like(
                        cb.lower(
                            root.join("dono", JoinType.INNER).get("nome")
                        ),
                        "%" + request.nomeDono().trim().toLowerCase() + "%"
                    )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return animalRepository.findAll(specification, pageable)
            .map(this::toResponse);
    }

    private Animal toEntity(AnimalCreateRequest request) {
        Animal animal = new Animal();

        animal.setNome(request.nome());
        animal.setEspecie(request.especie());
        animal.setDataNascimento(request.dataNascimento());
        animal.setSexo(request.sexo());
        animal.setPeso(request.peso());

        Usuario dono = usuarioRepository
            .findByIdAndStatus(request.idDono(), Status.ATIVO)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Dono não encontrado ou inativo"
            ));

        if (dono.getPermissao() != Permissao.CLIENTE) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O usuário informado não possui permissão de cliente"
            );
        }

        animal.setDono(dono);

        // Cria o prontuário associado ao animal
        Prontuario prontuario = new Prontuario();
        prontuarioRepository.save(prontuario);

        animal.setProntuario(prontuario);

        return animal;
    }

    @Transactional
    public AnimalResponse salvar(AnimalCreateRequest request) {
        Animal animal = toEntity(request);
        Animal salvo = animalRepository.save(animal);

        return toResponse(salvo);
    }

    private AnimalUpdateRequest toUpdateRequest(Animal animal) {
        return new AnimalUpdateRequest(
            animal.getNome(),
            animal.getEspecie(),
            animal.getDataNascimento(),
            animal.getSexo(),
            animal.getPeso()
        );
    }

    @Transactional(readOnly = true)
    public AnimalUpdateRequest buscarParaEdicao(Long id) {
        Animal animal = buscarAtivo(id);
        return toUpdateRequest(animal);
    }

    @Transactional
    public void atualizar(Long id, AnimalUpdateRequest request) {
        Animal animal = buscarAtivo(id);

        animal.setNome(request.nome());
        animal.setEspecie(request.especie());
        animal.setDataNascimento(request.dataNascimento());
        animal.setSexo(request.sexo());
        animal.setPeso(request.peso());

        // O dono e o prontuário existentes são preservados.
        animalRepository.save(animal);
    }

    @Transactional
    public void remover(Long id) {
        Animal animal = buscarAtivo(id);

        animal.setStatus(Status.INATIVO);

        animalRepository.save(animal);
    }
}