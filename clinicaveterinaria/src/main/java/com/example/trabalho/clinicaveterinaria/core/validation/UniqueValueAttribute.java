package com.example.trabalho.clinicaveterinaria.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import com.example.trabalho.clinicaveterinaria.core.validation.service.UniqueValue;
import com.example.trabalho.clinicaveterinaria.core.validation.validator.UniqueValueAttributeValidator;

/**
 * Anotação de validação customizada para garantir a unicidade do valor de um atributo (como CPF, e-mail, CNPJ, etc.).
 * <p>
 * Esta anotação deve ser aplicada em nível de <b>Classe ou Record (DTOs/Entidades)</b>.
 * Ela delega a lógica de verificação de unicidade no banco de dados para um serviço gerenciado pelo Spring 
 * que implemente a interface {@link UniqueValue}.
 * </p>
 * 
 * <h3>Comportamento com valores {@code null} ou em branco:</h3>
 * <ul>
 *   <li>Valores {@code null} ou cadeias de caracteres em branco são considerados <strong>válidos</strong> por esta anotação.</li>
 *   <li>A responsabilidade de exigir o preenchimento do campo é do Bean Validation através de anotações como
 *       {@link jakarta.validation.constraints.NotBlank @NotBlank} ou {@link jakarta.validation.constraints.NotNull @NotNull}.</li>
 * </ul>
 * 
 * <h3>Passo a passo de como utilizar (Exemplo com CPF):</h3>
 * 
 * <b>1. Crie a interface do serviço de validação estendendo {@link UniqueValue}:</b>
 * <pre>{@code
 * public interface CPFUnicoService extends UniqueValue {
 * }
 * }</pre>
 * 
 * <b>2. Crie a implementação do serviço anotada com {@code @Service}:</b>
 * <pre>{@code
 * @Service
 * public class CPFUnicoServiceImpl implements CPFUnicoService {
 * 
 *     private final PessoaRepository repositorio;
 * 
 *     public CPFUnicoServiceImpl(PessoaRepository repositorio) {
 *         this.repositorio = repositorio;
 *     }
 * 
 *     @Override
 *     public boolean isValueUnique(Object value, String attributeName, Object rootObject) {
 *         if (value == null) return true;
 *         String cpf = (String) value;
 * 
 *         if (rootObject instanceof PessoaCreateRequest) {
 *             return !repositorio.existsByCpfAndStatus(cpf, Status.ATIVO);
 *         }
 *         if (rootObject instanceof PessoaUpdateRequest) {
 *             return true;
 *         }
 *         throw new UnsupportedOperationException("Validação de CPF Único não suportada para: " + rootObject.getClass().getName());
 *     }
 * }
 * }</pre>
 * 
 * <b>3. Utilize a anotação no seu DTO (Classe ou Record):</b>
 * <pre>{@code
 * @UniqueValueAttribute(
 *     attribute = "cpf",
 *     service = CPFUnicoService.class,
 *     message = "Já existe uma pessoa cadastrada com este CPF"
 * )
 * public record PessoaCreateRequest(
 *     String nome,
 *     String cpf
 * ) {}
 * }</pre>
 * 
 * @see UniqueValue
 * @see UniqueValueAttributeValidator
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValueAttributeValidator.class)
@Documented
@Repeatable(UniqueValueAttribute.List.class)
public @interface UniqueValueAttribute {

    /**
     * Nome do atributo do DTO/Classe cujo valor será extraído e validado quanto à unicidade.
     *
     * @return o nome do atributo.
     */
    String attribute();

    /**
     * Mensagem de erro a ser exibida caso o valor já esteja cadastrado no sistema.
     *
     * @return a mensagem de validação padrão.
     */
    String message() default "O valor informado já está em uso";

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
     * Interface do serviço Spring (que estende {@link UniqueValue}) responsável por executar a consulta de unicidade no banco de dados.
     *
     * @return a classe da interface do serviço.
     */
    Class<? extends UniqueValue> service();

    /**
     * Qualificador opcional do Bean do Spring ({@code @Qualifier}), caso existam múltiplas implementações para a mesma interface de serviço.
     *
     * @return o nome do bean qualificador.
     */
    String serviceQualifier() default "";

    /**
     * Container para permitir a repetição da anotação {@link UniqueValueAttribute} no mesmo elemento.
     */
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        UniqueValueAttribute[] value();
    }
}
