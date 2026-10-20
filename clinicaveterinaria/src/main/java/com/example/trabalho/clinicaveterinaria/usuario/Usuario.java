package com.example.trabalho.clinicaveterinaria.usuario;

import java.io.Serializable;

import org.hibernate.annotations.DynamicUpdate;

import com.example.trabalho.clinicaveterinaria.common.enums.Permissao;
import com.example.trabalho.clinicaveterinaria.common.enums.Status;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Entity 
@Table (name = "usuario")
@DynamicUpdate
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(name="geradorusuario", sequenceName="usuario_id_seq", allocationSize=1)
	@GeneratedValue(generator="geradorusuario", strategy=GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    private String nome;

    @EqualsAndHashCode.Include
    @ToString.Include
    private String email;

    private String senha;

    @Enumerated (EnumType.STRING)
    @ToString.Include
    private Permissao permissao;

    @Enumerated (EnumType.STRING)
    @ToString.Include
    private Status status = Status.ATIVO;
}
