package com.example.trabalho.clinicaveterinaria.usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;
import com.example.trabalho.clinicaveterinaria.common.enums.Status;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    public Page<Usuario> findAllByStatus(Status status, Pageable pageable);
    public Optional<Usuario> findByIdAndStatus(Long id, Status status);
    
    public List<Usuario> findTop10ByNomeContainingIgnoreCaseAndPermissaoAndStatusOrderByNomeAsc(String nome, Permissao permissao, Status status);
    
} 