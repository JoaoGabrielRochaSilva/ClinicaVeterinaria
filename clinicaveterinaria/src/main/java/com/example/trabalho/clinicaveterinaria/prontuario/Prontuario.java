package com.example.trabalho.clinicaveterinaria.prontuario;

import java.io.Serializable;

import org.hibernate.annotations.DynamicUpdate;

import com.example.trabalho.clinicaveterinaria.common.enums.Status;

import jakarta.persistence.Column;
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
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity 
@Table (name = "prontuario")
@DynamicUpdate
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor 
public class Prontuario implements Serializable {

    private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(name="geradorprontuario", sequenceName="prontuario_id_seq", allocationSize=1)
	@GeneratedValue(generator="geradorprontuario", strategy=GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    private String alergias;

    @Column (name = "doencas_pre_existentes")
    @ToString.Include
    private String doencasPreExistentes;

    @ToString.Include
    private String observacoes;

    @Enumerated (EnumType.STRING)
    @ToString.Include
    private Status status = Status.ATIVO;
}
