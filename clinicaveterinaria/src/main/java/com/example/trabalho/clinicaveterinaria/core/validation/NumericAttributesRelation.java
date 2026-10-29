package com.example.trabalho.clinicaveterinaria.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import com.example.trabalho.clinicaveterinaria.core.validation.validator.NumericAttributesRelationValidator;

/**
 * Anotação de validação em nível de classe/tipo utilizada para verificar a relação numérica 
 * matemática entre dois atributos numéricos ({@link Number}) de um mesmo objeto.
 * <p>
 * Permite validar se um atributo numérico é igual, diferente, maior, menor, maior ou igual, 
 * ou menor ou igual a outro atributo, com suporte para margem de tolerância ({@code epsilon}).
 * </p>
 * 
 * <h3>Comportamento com valores {@code null}:</h3>
 * <ul>
 *   <li>Se o objeto for {@code null} ou se qualquer um dos dois atributos avaliados for {@code null},
 *       a validação é considerada <strong>válida</strong>.</li>
 *   <li>Para tornar o preenchimento dos campos obrigatório, combine esta validação com anotações de campo 
 *       como {@link jakarta.validation.constraints.NotNull @NotNull}.</li>
 * </ul>
 * 
 * <h3>Margem de Tolerância ({@code epsilon}):</h3>
 * <p>
 * A comparação é realizada internamente utilizando {@link java.math.BigDecimal} para evitar problemas de 
 * precisão com ponto flutuante. O parâmetro {@link #epsilon()} define a margem de tolerância aceitável 
 * durante a comparação.
 * </p>
 * 
 * <h3>Exemplo de Uso em DTO baseado em Classe tradicional:</h3>
 * <pre>{@code
 * @NumericAttributesRelation(
 *     attribute1 = "precoPromocional",
 *     attribute2 = "precoOriginal",
 *     relation = AttributesRelations.LESSTHAN,
 *     message = "O preço promocional deve ser estritamente menor que o preço original"
 * )
 * public class ProdutoRequestDTO {

 *     private Double precoOriginal;
 *     private Double precoPromocional;

 *     // Getters e Setters
 * }
 * }</pre>
 *
 * <h3>Exemplo de Uso em DTO baseado em Record (Java 14+):</h3>
 * <pre>{@code
 * @NumericAttributesRelation(
 *     attribute1 = "quantidadeMaxima",
 *     attribute2 = "quantidadeMinima",
 *     relation = AttributesRelations.GREATEROREQUAL,
 *     message = "A quantidade máxima não pode ser inferior à quantidade mínima"
 * )
 * public record EstoqueRecordDTO(
 *     Integer quantidadeMinima,
 *     Integer quantidadeMaxima
 * ) {}
 * }</pre>
 *
 * <h3>Exemplo de Uso Múltiplo (Repetível):</h3>
 * <pre>{@code
 * @NumericAttributesRelation(
 *     attribute1 = "descontoMaximo",
 *     attribute2 = "descontoMinimo",
 *     relation = AttributesRelations.GREATEROREQUAL,
 *     message = "O desconto máximo deve ser maior ou igual ao mínimo"
 * )
 * @NumericAttributesRelation(
 *     attribute1 = "precoFinal",
 *     attribute2 = "precoCusto",
 *     relation = AttributesRelations.GREATERTHAN,
 *     message = "O preço final deve ser superior ao preço de custo"
 * )
 * public record TabelaPrecosRecordDTO(
 *     BigDecimal descontoMinimo,
 *     BigDecimal descontoMaximo,
 *     BigDecimal precoCusto,
 *     BigDecimal precoFinal
 * ) {}
 * }</pre>

 * @see NumericAttributesRelationValidator
 * @see AttributesRelations
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NumericAttributesRelationValidator.class)
@Documented
@Repeatable(NumericAttributesRelation.List.class)
public @interface NumericAttributesRelation {

	/**
	 * Nome do primeiro atributo numérico da comparação (operando à esquerda).
	 *
	 * @return o nome do primeiro campo numérico.
	 */
	String attribute1();

	/**
	 * Nome do segundo atributo numérico da comparação (operando à direita).
	 * <p>
	 * Em caso de erro de validação, a mensagem será associada diretamente a este atributo no Bean Validation.
	 * </p>
	 *
	 * @return o nome do segundo campo numérico.
	 */
	String attribute2();

	/**
	 * Define a relação matemática que deve ser satisfeita entre {@link #attribute1()} e {@link #attribute2()}.
	 *
	 * @return a relação numérica desejada do enum {@link AttributesRelations}.
	 */
	AttributesRelations relation();

	/**
	 * Define a margem de erro/tolerância para comparações entre números de ponto flutuante.
	 * <p>
	 * O valor padrão é {@code 0.0001}.
	 * </p>
	 *
	 * @return a margem de tolerância.
	 */
	double epsilon() default 0.0001;

	/**
	 * Mensagem de erro a ser exibida caso a relação numérica definida não seja satisfeita.
	 *
	 * @return a mensagem de validação padrão.
	 */
	String message() default "A relação numérica entre os atributos é inválida";

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
	 * Container para permitir a repetição da anotação {@link NumericAttributesRelation} na mesma classe/record.
	 */
	@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		NumericAttributesRelation[] value();
	}
}
