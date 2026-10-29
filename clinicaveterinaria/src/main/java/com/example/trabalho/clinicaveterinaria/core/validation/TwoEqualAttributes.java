package com.example.trabalho.clinicaveterinaria.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import com.example.trabalho.clinicaveterinaria.core.validation.validator.TwoEqualAttributesValidator;

/**
 * Anotação de validação em nível de classe/tipo utilizada para verificar se dois atributos 
 * de um mesmo objeto possuem valores iguais.
 * <p>
 * É comumente empregada em formulários de cadastro e alteração para comparar campos de confirmação,
 * tais como "senha" e "confirmação de senha" ou "e-mail" e "confirmação de e-mail".
 * </p>
 * 
 * <h3>Comportamento de Igualdade:</h3>
 * <ul>
 *   <li>Se ambos os atributos forem {@code null}, a validação é considerada <strong>válida</strong>.</li>
 *   <li>A comparação é feita via reflexão utilizando o método {@code equals()} entre o valor do 
 *       {@link #attribute1()} e o valor do {@link #attribute2()}.</li>
 *   <li>Em caso de divergência, a violação de validação é automaticamente vinculada ao campo apontado por {@link #attribute2()}.</li>
 * </ul>
 * 
 * <h3>Anotação Repetível:</h3>
 * <p>
 * Por ser uma anotação {@link Repeatable}, você pode declarar múltiplas instâncias de {@code @TwoEqualAttributes} 
 * na mesma classe ou record para validar diferentes pares de atributos simultaneamente.
 * </p>
 *
 * <h3>Exemplo de Uso em DTO baseado em Classe tradicional:</h3>
 * <pre>{@code
 * @TwoEqualAttributes(
 *     attribute1 = "senha",
 *     attribute2 = "confirmacaoSenha",
 *     message = "A senha e a confirmação de senha devem ser iguais"
 * )
 * public class UsuarioRequestDTO {

 *     private String senha;
 *     private String confirmacaoSenha;

 *     // Getters e Setters
 * }
 * }</pre>
 *
 * <h3>Exemplo de Uso em DTO baseado em Record (Java 14+):</h3>
 * <pre>{@code
 * @TwoEqualAttributes(
 *     attribute1 = "email",
 *     attribute2 = "confirmacaoEmail",
 *     message = "Os e-mails informados não coincidem"
 * )
 * public record UsuarioRecordDTO(
 *     String email,
 *     String confirmacaoEmail
 * ) {}
 * }</pre>
 *
 * <h3>Exemplo de Uso Múltiplo (Repetível):</h3>
 * <pre>{@code
 * @TwoEqualAttributes(attribute1 = "email", attribute2 = "confirmacaoEmail", message = "E-mails divergentes")
 * @TwoEqualAttributes(attribute1 = "senha", attribute2 = "confirmacaoSenha", message = "Senhas divergentes")
 * public record RegistroCompletoRecordDTO(
 *     String email,
 *     String confirmacaoEmail,
 *     String senha,
 *     String confirmacaoSenha
 * ) {}
 * }</pre>
 *
 * @see TwoEqualAttributesValidator
 */
@Constraint(validatedBy = TwoEqualAttributesValidator.class)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(TwoEqualAttributes.List.class)
public @interface TwoEqualAttributes {

	/**
	 * Mensagem de erro a ser exibida caso os dois atributos não sejam iguais.
	 *
	 * @return a mensagem de validação padrão.
	 */
	String message() default "Os valores são diferentes";

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
	 * Nome do primeiro atributo a ser comparado (atributo base).
	 *
	 * @return o nome do primeiro campo.
	 */
	String attribute1();

	/**
	 * Nome do segundo atributo a ser comparado (atributo de confirmação).
	 * <p>
	 * Em caso de erro de validação, a mensagem será associada diretamente a este atributo no Bean Validation.
	 * </p>
	 *
	 * @return o nome do segundo campo.
	 */
	String attribute2();

	/**
	 * Container para permitir a repetição da anotação {@link TwoEqualAttributes} na mesma classe/record.
	 */
	@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		TwoEqualAttributes[] value();
	}
}
