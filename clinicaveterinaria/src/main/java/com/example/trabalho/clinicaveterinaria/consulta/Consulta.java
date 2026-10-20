package com.example.trabalho.clinicaveterinaria.consulta;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import com.example.trabalho.clinicaveterinaria.agendamento.Agendamento;
import com.example.trabalho.clinicaveterinaria.common.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity 
@Table (name = "consulta")
@DynamicUpdate
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor 
public class Consulta implements Serializable {

    private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(name="geradorconsulta", sequenceName="consulta_id_seq", allocationSize=1)
	@GeneratedValue(generator="geradorconsulta", strategy=GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    @Column (name = "data_hora")
    private LocalDateTime dataHora; //Data e hora que ocorreu a consulta

    @ToString.Include
    private String diagnostico;

    @ToString.Include
    private String tratamento;

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "agendamento_id")
    private Agendamento agendamento;

    @Enumerated (EnumType.STRING)
    @ToString.Include
    private Status status = Status.ATIVO;
}
