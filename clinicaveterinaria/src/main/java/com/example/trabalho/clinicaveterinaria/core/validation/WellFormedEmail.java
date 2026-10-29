package com.example.trabalho.clinicaveterinaria.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import com.example.trabalho.clinicaveterinaria.core.validation.validator.WellFormedEmailValidator;

/**
 * Anotação de validação para verificar se uma {@link String} representa um endereço de e-mail bem-formatado.
 * <p>
 * A verificação é delegada ao {@link WellFormedEmailValidator}, que utiliza a biblioteca
 * Apache Commons {@code EmailValidator} para garantir a conformidade com os padrões de sintaxe de e-mail.
 * </p>
 * 
 * <h3>Comportamento com valores {@code null} ou em branco:</h3>
 * <ul>
 *   <li>Valores {@code null}, vazios ({@code ""}) ou formados apenas por espaços em branco são considerados <strong>válidos</strong>.</li>
 *   <li>Essa escolha segue o padrão do Bean Validation para permitir a composição de restrições. Para tornar o campo obrigatório, 
 *       combine esta anotação com {@link jakarta.validation.constraints.NotBlank @NotBlank} ou 
 *       {@link jakarta.validation.constraints.NotNull @NotNull}.</li>
 * </ul>
 *
 * <h3>Exemplo de Uso em DTO baseado em Classe tradicional:</h3>
 * <pre>{@code
 * public class UsuarioRequestDTO {
 *
 *     @NotBlank(message = "O e-mail é obrigatório")
 *     @WellFormedEmail(message = "Por favor, informe um e-mail válido")
 *     private String email;

 *     public String getEmail() {
 *         return email;
 *     }

 *     public void setEmail(String email) {
 *         this.email = email;
 *     }
 * }
 * }</pre>
 *
 * <h3>Exemplo de Uso em DTO baseado em Record (Java 14+):</h3>
 * <pre>{@code
 * public record UsuarioRecordDTO(
 *     @NotBlank(message = "O nome é obrigatório")
 *     String nome,
 *
 *     @NotBlank(message = "O e-mail é obrigatório")
 *     @WellFormedEmail(message = "Por favor, informe um e-mail válido")
 *     String email
 * ) {}
 * }</pre>
 *
 * <h3>Exemplo de Uso em Parâmetros de Controllers:</h3>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/usuarios")
 * @Validated
 * public class UsuarioController {
 *
 *     @GetMapping("/buscar")
 *     public ResponseEntity<Usuario> buscarPorEmail(
 *             @RequestParam @WellFormedEmail String email) {
 *         // Lógica do método
 *     }
 * }
 * }</pre>
 *
 * @see WellFormedEmailValidator
 * @see org.apache.commons.validator.routines.EmailValidator
 */

@Constraint(validatedBy = WellFormedEmailValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE })
@Documented
@Repeatable(WellFormedEmail.List.class)
public @interface WellFormedEmail {

	public String message() default "O formato do e-mail não é válido";

	public Class<?>[] groups() default {};

	public Class<? extends Payload>[] payload() default {};

	@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		WellFormedEmail[] value();
	}

}
