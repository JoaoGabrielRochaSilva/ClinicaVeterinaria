package com.example.trabalho.clinicaveterinaria.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import com.example.trabalho.clinicaveterinaria.core.validation.validator.ValidPasswordValidator;

/**
 * Anotação de validação customizada para aplicação de políticas de segurança em senhas (baseada na biblioteca Passay).
 * <p>
 * Por padrão, esta anotação adota o princípio de <i>"Convenção sobre Configuração"</i>, vindo pré-configurada
 * de fábrica com o mínimo recomendado pelo mercado (tamanho entre 8 e 32 caracteres e bloqueio de senhas comuns).
 * </p>
 * 
 * <h3>Comportamento com valores {@code null}:</h3>
 * <ul>
 *   <li>Valores {@code null} são considerados <strong>válidos</strong> por esta anotação.</li>
 *   <li>Essa escolha segue o padrão do Bean Validation para permitir a composição de restrições.
 *       Para tornar o preenchimento da senha obrigatório, combine esta anotação com 
 *       {@link jakarta.validation.constraints.NotBlank @NotBlank} ou 
 *       {@link jakarta.validation.constraints.NotNull @NotNull}.</li>
 * </ul>
 * 
 * <h3>Pré-requisito (Dependência Maven):</h3>
 * <pre>{@code
 * <dependency>
 *     <groupId>org.passay</groupId>
 *     <artifactId>passay</artifactId>
 *     <version>2.0.0</version>
 * </dependency>
 * }</pre>
 * 
 * <h3>Exemplo de Uso em DTO baseado em Classe tradicional:</h3>
 * <pre>{@code
 * public class UsuarioRequestDTO {
 *
 *     @NotBlank(message = "A senha é obrigatória")
 *     @ValidPassword(
 *         minSize = 8,
 *         digits = 1,
 *         uppercase = 1,
 *         special = 1,
 *         additionalForbidden = {"vacinacao2026", "empresa123"}
 *     )
 *     private String senha;

 *     public String getSenha() {
 *         return senha;
 *     }

 *     public void setSenha(String senha) {
 *         this.senha = senha;
 *     }
 * }
 * }</pre>
 *
 * <h3>Exemplo de Uso em DTO baseado em Record (Java 14+):</h3>
 * <pre>{@code
 * public record UsuarioRecordDTO(
 *     @NotBlank(message = "O login é obrigatório")
 *     String login,
 *
 *     @NotBlank(message = "A senha é obrigatória")
 *     @ValidPassword(
 *         minSize = 10,
 *         digits = 2,
 *         special = 1,
 *         sequences = 3
 *     )
 *     String senha
 * ) {}
 * }</pre>
 *
 * @author Rafael Godoi Orbolato
 * @since 1.0.0
 * @see ValidPasswordValidator
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPasswordValidator.class)
@Documented
@Repeatable(ValidPassword.List.class)
public @interface ValidPassword {

	/**
	 * Define o tamanho mínimo permitido para a senha.
	 * <p>
	 * O padrão de mercado é {@code 8}. Atribua o valor {@code 0} para desativar a validação de tamanho mínimo.
	 * </p>
	 * 
	 * @return o tamanho mínimo da senha.
	 */
	int minSize() default 8;

	/**
	 * Define o tamanho máximo permitido para a senha.
	 * <p>
	 * O padrão é {@code 32} (evita estouro de memória no processamento do hash). Atribua {@code 0} para desativar o limite máximo.
	 * </p>
	 * 
	 * @return o tamanho máximo da senha.
	 */
	int maxSize() default 32;

	/**
	 * Define se a lista de senhas proibidas e comuns (ex: "123456", "password", "admin", "senha") deve ser validada.
	 * <p>
	 * O padrão é {@code true}. Defina como {@code false} em ambiente de desenvolvimento para facilitar testes com senhas simples.
	 * </p>
	 * 
	 * @return {@code true} se deve barrar senhas comuns; {@code false} caso contrário.
	 */
	boolean validateForbidden() default true;

	/**
	 * Quantidade mínima obrigatória de letras alfabéticas gerais (maiúsculas ou minúsculas).
	 * <p>
	 * O valor {@code 0} indica que a regra está desativada.
	 * </p>
	 * 
	 * @return quantidade mínima de letras.
	 */
	int letters() default 0;

	/**
	 * Quantidade mínima obrigatória de dígitos numéricos (0-9).
	 * <p>
	 * O valor {@code 0} indica que a regra está desativada.
	 * </p>
	 * 
	 * @return quantidade mínima de dígitos.
	 */
	int digits() default 0;

	/**
	 * Quantidade mínima obrigatória de caracteres especiais (ex: {@code !}, {@code @}, {@code #}, {@code $}, etc.).
	 * <p>
	 * O valor {@code 0} indica que a regra está desativada.
	 * </p>
	 * 
	 * @return quantidade mínima de caracteres especiais.
	 */
	int special() default 0;

	/**
	 * Quantidade mínima obrigatória de caracteres alfabéticos em letra MAIÚSCULA.
	 * <p>
	 * O valor {@code 0} indica que a regra está desativada.
	 * </p>
	 * 
	 * @return quantidade mínima de maiúsculas.
	 */
	int uppercase() default 0;

	/**
	 * Quantidade mínima obrigatória de caracteres alfabéticos em letra minúscula.
	 * <p>
	 * O valor {@code 0} indica que a regra está desativada.
	 * </p>
	 * 
	 * @return quantidade mínima de minúsculas.
	 */
	int lowercase() default 0;

	/**
	 * Define o limite de caracteres consecutivos em sequência lógica (alfabética ou numérica).
	 * <p>
	 * Exemplo: Se definido como {@code 3}, senhas contendo "abc" ou "123" serão rejeitadas. O valor {@code 0} desativa a regra.
	 * </p>
	 * 
	 * @return o tamanho limite da sequência lógica consecutiva.
	 */
	int sequences() default 0;

	/**
	 * Define o limite de caracteres consecutivos em sequência física no teclado (layout QWERTY).
	 * <p>
	 * Exemplo: Se definido como {@code 4}, senhas contendo "qwer" ou "asdf" serão rejeitadas. O valor {@code 0} desativa a regra.
	 * </p>
	 * 
	 * @return o tamanho limite da sequência de teclas.
	 */
	int keyboardSequences() default 0;

	/**
	 * Lista de palavras adicionais específicas que devem ser proibidas nesta validação (ex: nome do sistema ou da empresa).
	 * 
	 * @return array contendo termos banidos adicionais.
	 */
	String[] additionalForbidden() default {};

	/**
	 * Mensagem de erro customizada. Se deixada em branco, o validador gerará dinamicamente uma mensagem detalhando cada regra violada.
	 * 
	 * @return a mensagem de erro.
	 */
	String message() default "";

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
	 * Define um container para permitir a repetição da anotação {@link ValidPassword} no mesmo elemento.
	 */
	@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		ValidPassword[] value();
	}
}
