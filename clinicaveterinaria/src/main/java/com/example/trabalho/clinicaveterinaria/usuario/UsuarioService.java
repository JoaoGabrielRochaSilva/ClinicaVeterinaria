package com.example.trabalho.clinicaveterinaria.usuario;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.trabalho.clinicaveterinaria.agendamento.AgendamentoRepository;
import com.example.trabalho.clinicaveterinaria.animal.AnimalRepository;
import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;
import com.example.trabalho.clinicaveterinaria.common.enums.Status;
import com.example.trabalho.clinicaveterinaria.common.exception.RegraDeNegocioException;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioCreateRequest;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioResponse;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioSearchRequest;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioUpdateRequest;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AnimalRepository animalRepository;

    @Transactional
    public Usuario salvar(UsuarioCreateRequest request) {
        Usuario usuario = toEntity(request);
        return usuarioRepository.save(usuario);
    }

    private Usuario toEntity(UsuarioCreateRequest request) {
        Usuario usuario = new Usuario();

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(request.senha());
        usuario.setPermissao(request.permissao());

        return usuario;
    }

    private Usuario buscarAtivo(Long id) {
        return usuarioRepository
                .findByIdAndStatus(id, Status.ATIVO)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuário não encontrado ou inativo"
                ));
    }

    @Transactional(readOnly = true)
    public Usuario buscarAtivoComPermissao(Long id, Permissao permissao) {
        Usuario usuario = buscarAtivo(id);

        if (usuario.getPermissao() != permissao) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O usuário informado não possui a permissão " + permissao
            );
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return toResponse(buscarAtivo(id));
    }

    private Usuario toEntity(UsuarioSearchRequest request) {
        Usuario usuario = new Usuario();

        usuario.setId(request.id());
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPermissao(request.permissao());

        return usuario;
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPermissao()
        );
    }

    public Page<UsuarioResponse> pesquisar(
            UsuarioSearchRequest request,
            Pageable pageable) {

        if (request.isEmpty()) {
            return usuarioRepository.findAllByStatus(Status.ATIVO, pageable)
                    .map(this::toResponse);
        }

        Usuario usuarioFiltro = toEntity(request);
        usuarioFiltro.setStatus(Status.ATIVO);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(
                    ExampleMatcher.StringMatcher.CONTAINING
                );

        return usuarioRepository.findAll(
                    Example.of(usuarioFiltro, matcher),
                    pageable
                )
                .map(this::toResponse);
    }

    public List<UsuarioResponse> buscarPeloNome(
            String nome,
            Permissao permissao) {

        return usuarioRepository
                .findTop10ByNomeContainingIgnoreCaseAndPermissaoAndStatusOrderByNomeAsc(
                    nome,
                    permissao,
                    Status.ATIVO
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Só nome e e-mail mudam (permissão e senha não são editáveis)
    @Transactional
    public void atualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = buscarAtivo(id);

        // O próprio e-mail é permitido; o de outro usuário não
        if (usuarioRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new RegraDeNegocioException("email", "Já existe um usuário cadastrado com este e-mail");
        }

        usuario.setNome(request.nome().trim());
        usuario.setEmail(request.email());

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void remover(Long id) {
        Usuario usuario = buscarAtivo(id);

        usuario.setStatus(Status.INATIVO);
        usuarioRepository.save(usuario);

        // Cascatas por query em massa
        if (usuario.getPermissao() == Permissao.VETERINARIO) {
            agendamentoRepository.inativarDoVeterinario(id);
        } else if (usuario.getPermissao() == Permissao.CLIENTE) {
            agendamentoRepository.inativarDoCliente(id);
            animalRepository.inativarDoDono(id);
        }
    }
}