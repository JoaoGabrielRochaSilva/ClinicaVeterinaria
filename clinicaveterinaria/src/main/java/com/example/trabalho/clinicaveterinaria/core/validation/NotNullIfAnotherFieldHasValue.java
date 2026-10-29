package com.example.trabalho.clinicaveterinaria.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import com.example.trabalho.clinicaveterinaria.core.validation.validator.NotNullIfAnotherFieldHasValueValidator;

/**
 * Anotação de validação condicional em nível de classe/tipo que exige que o campo informado em
 * {@link #dependFieldName()} não seja nulo ou em branco caso o campo informado em {@link #fieldName()}
 * possua o valor especificado em {@link #fieldValue()}.
 * <p>
 * É útil para tratar campos que se tornam de preenchimento obrigatório apenas quando uma determinada
 * opção é selecionada (por exemplo, exigir o campo "CNPJ" se o "tipoPessoa" for "JURIDICA").
 * </p>
 *
 * <h3>Regras de Validação Condicional:</h3>
 * <ul>
 *   <li>Se a condição for atendida e o campo dependente for {@code null} ou uma {@link String} vazia/com espaços em branco, a validação falhará.</li>
 *   <li>Em caso de falha, a violação de validação é vinculada diretamente ao atributo especificado em {@link #dependFieldName()}.</li>
 * </ul>
 *
 * <h3>Exemplo de Uso em DTO baseado em Classe tradicional:</h3>
 * <pre>{@code
 * @NotNullIfAnotherFieldHasValue(
 *     fieldName = "tipoPessoa",
 *     fieldValue = "JURIDICA",
 *     dependFieldName = "cnpj",
 *     message = "O CNPJ é obrigatório para pessoa jurídica"
 * )
 * public class ClienteRequestDTO {

 *     private String tipoPessoa; // "FISICA" ou "JURIDICA"
 *     private String cnpj;

 *     // Getters e Setters
 * }
 * }</pre>
 *
 * <h3>Exemplo de Uso em DTO baseado em Record (Java 14+):</h3>
 * <pre>{@code
 * @NotNullIfAnotherFieldHasValue(
 *     fieldName = "possuiAlergia",
 *     fieldValue = "true",
 *     dependFieldName = "descricaoAlergia",
 *     message = "A descrição da alergia é obrigatória quando informado que possui alergia"
 * )
 * public record FichaMedicaRecordDTO(
 *     Boolean possuiAlergia,
 *     String descricaoAlergia
 * ) {}
 * }</pre>
 *
 * <h3>Exemplo de Uso Múltiplo (Repetível):</h3>
 * <pre>{@code
 * @NotNullIfAnotherFieldHasValue(
 *     fieldName = "nacionalidade",
 *     fieldValue = "ESTRANGEIRO",
 *     dependFieldName = "passaporte",
 *     message = "O passaporte é obrigatório para estrangeiros"
 * )
 * @NotNullIfAnotherFieldHasValue(
 *     fieldName = "nacionalidade",
 *     fieldValue = "BRASILEIRO",
 *     dependFieldName = "cpf",
 *     message = "O CPF é obrigatório para brasileiros"
 * )
 * public record PassagemRecordDTO(
 *     String nacionalidade,
 *     String cpf,
 *     String passaporte
 * ) {}
 * }</pre>
 *
 * @see NotNullIfAnotherFieldHasValueValidator
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotNullIfAnotherFieldHasValueValidator.class)
@Documented
@Repeatable(NotNullIfAnotherFieldHasValue.List.class)
public @interface NotNullIfAnotherFieldHasValue {

	/**
	 * Nome do campo principal cuja presença do valor gatilho ativará a obrigatoriedade.
	 *
	 * @return o nome do campo gatilho.
	 */
	String fieldName();

	/**
	 * Valor esperado no campo {@link #fieldName()} para ativar a validação do campo dependente.
	 * <p>
	 * O valor informado é comparado com a representação em texto ({@code toString()}) do campo principal.
	 * Se for passada uma string vazia ({@code ""}), a obrigatoriedade será ativada caso o campo principal seja {@code null}.
	 * </p>
	 *
	 * @return a representação em texto do valor esperado.
	 */
	String fieldValue();

	/**
	 * Nome do campo dependente que se tornará obrigatoriamente preenchido caso a condição seja satisfeita.
	 *
	 * @return o nome do campo condicionalmente obrigatório.
	 */
	String dependFieldName();

	/**
	 * Mensagem de erro a ser exibida caso o campo dependente esteja nulo ou em branco quando a condição for atendida.
	 *
	 * @return a mensagem de validação padrão.
	 */
	String message() default "Campo de preenchimento obrigatório";

	/**
	 * Permite especificar grupos de validação aos quais esta restrição pertence.
	 *
	 * @return os grupos de validação.
	 */
	Class<?>[] groups() default {};

	/**
	 * Pode ser utilizado por clientes da API de Bean Validation para associar metadados à restrição.
	 *
	 * @return a carga útil (payload).
	 */
	Class<? extends Payload>[] payload() default {};

	/**
	 * Container para permitir a repetição da anotação {@link NotNullIfAnotherFieldHasValue} na mesma classe/record.
	 */
	@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		NotNullIfAnotherFieldHasValue[] value();
	}
}
