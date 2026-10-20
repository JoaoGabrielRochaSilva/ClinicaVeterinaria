package com.example.trabalho.clinicaveterinaria.agendamento;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import com.example.trabalho.clinicaveterinaria.animal.Animal;
import com.example.trabalho.clinicaveterinaria.common.enums.StatusAgendamento;
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
@Table (name = "agendamento")
@DynamicUpdate
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor 
public class Agendamento implements Serializable {

    private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(name="geradoragendamento", sequenceName="agendamento_id_seq", allocationSize=1)
	@GeneratedValue(generator="geradoragendamento", strategy=GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ToString.Include
    @Column (name = "data_hora")
    private LocalDateTime dataHora;//Data e hora que a consulta foi marcada

    @ToString.Include
    private String motivo;

    @Enumerated (EnumType.STRING)
    @ToString.Include
    private StatusAgendamento statusAgendamento = StatusAgendamento.PENDENTE;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "animal_id")
    private Animal animal;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "usuario_id")
    private Usuario veterinario;

}
