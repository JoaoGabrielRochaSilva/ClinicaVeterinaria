package com.example.trabalho.clinicaveterinaria.animal;

import java.io.Serializable;
import java.time.LocalDate;

import org.hibernate.annotations.DynamicUpdate;

import com.example.trabalho.clinicaveterinaria.common.enums.Status;
import com.example.trabalho.clinicaveterinaria.usuario.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity 
@Table (name = "animal")
@DynamicUpdate
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor 
public class Animal implements Serializable {

    private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(name="geradoranimal", sequenceName="animal_id_seq", allocationSize=1)
	@GeneratedValue(generator="geradoranimal", strategy=GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    private String nome;

    @ToString.Include
    private String especie;

    @Column (name = "data_nascimento")
    @ToString.Include
    private LocalDate dataNascimento;

    @ToString.Include
    private String sexo;

    @ToString.Include
    private Double peso;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario dono;

    @Enumerated (EnumType.STRING)
    @ToString.Include
    private Status status = Status.ATIVO;

}
