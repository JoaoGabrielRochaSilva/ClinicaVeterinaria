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

import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;
import com.example.trabalho.clinicaveterinaria.common.enums.Status;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioCreateRequest;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioResponse;
import com.example.trabalho.clinicaveterinaria.usuario.dto.UsuarioSearchRequest;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

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


    @Transactional
    public void remover(Long id) {
        Usuario usuario = usuarioRepository
                .findByIdAndStatus(id, Status.ATIVO)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuário não encontrado"
                ));

        usuario.setStatus(Status.INATIVO);
        usuarioRepository.save(usuario);
    }
}

