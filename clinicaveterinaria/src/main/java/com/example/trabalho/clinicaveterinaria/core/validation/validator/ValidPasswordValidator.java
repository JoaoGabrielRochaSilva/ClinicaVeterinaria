package com.example.trabalho.clinicaveterinaria.core.validation.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.passay.DefaultPasswordValidator;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResultDetail;
import org.passay.ValidationResult;
import org.passay.data.EnglishCharacterData;
import org.passay.data.EnglishSequenceData;
import org.passay.dictionary.ArrayWordList;
import org.passay.dictionary.WordListDictionary;
import org.passay.rule.CharacterRule;
import org.passay.rule.DictionaryRule;
import org.passay.rule.IllegalSequenceRule;
import org.passay.rule.LengthRule;
import org.passay.rule.Rule;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.example.trabalho.clinicaveterinaria.core.validation.ValidPassword;

/**
 * Implementação do validador associado à anotação {@link ValidPassword}.
 */
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private PasswordValidator validator;
    private int letters;
    private int digits;
    private int special;
    private int minSize;
    private int maxSize;
    private int uppercase;
    private int lowercase;
    private String annotationMessage;

    private static final List<String> DEFAULT_FORBIDDEN = Arrays.asList(
            "12345", "123456", "123456789", "password", "qwerty", "111111",
            "12345678", "senha", "admin", "mudar123");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.letters = constraintAnnotation.letters();
        this.digits = constraintAnnotation.digits();
        this.special = constraintAnnotation.special();
        this.minSize = constraintAnnotation.minSize();
        this.maxSize = constraintAnnotation.maxSize();
        this.uppercase = constraintAnnotation.uppercase();
        this.lowercase = constraintAnnotation.lowercase();
        this.annotationMessage = constraintAnnotation.message();

        List<Rule> rules = new ArrayList<>();

        if (minSize > 0 || maxSize > 0) {
            int max = (maxSize > 0) ? maxSize : Integer.MAX_VALUE;
            rules.add(new LengthRule(minSize, max));
        }

        if (letters > 0)
            rules.add(new CharacterRule(EnglishCharacterData.Alphabetical, letters));
        if (digits > 0)
            rules.add(new CharacterRule(EnglishCharacterData.Digit, digits));
        if (special > 0)
            rules.add(new CharacterRule(EnglishCharacterData.Special, special));
        if (uppercase > 0)
            rules.add(new CharacterRule(EnglishCharacterData.UpperCase, uppercase));
        if (lowercase > 0)
            rules.add(new CharacterRule(EnglishCharacterData.LowerCase, lowercase));

        if (constraintAnnotation.sequences() > 0) {
            rules.add(new IllegalSequenceRule(EnglishSequenceData.Alphabetical, constraintAnnotation.sequences(), false));
            rules.add(new IllegalSequenceRule(EnglishSequenceData.Numerical, constraintAnnotation.sequences(), false));
        }

        if (constraintAnnotation.keyboardSequences() > 0) {
            rules.add(new IllegalSequenceRule(EnglishSequenceData.USQwerty, constraintAnnotation.keyboardSequences(), false));
        }

        if (constraintAnnotation.validateForbidden()) {
            List<String> forbiddenPasswords = new ArrayList<>(DEFAULT_FORBIDDEN);
            if (constraintAnnotation.additionalForbidden().length > 0) {
                forbiddenPasswords.addAll(Arrays.asList(constraintAnnotation.additionalForbidden()));
            }
            String[] forbiddenArray = forbiddenPasswords.toArray(new String[0]);
            WordListDictionary dictionary = new WordListDictionary(new ArrayWordList(forbiddenArray, false));
            rules.add(new DictionaryRule(dictionary));
        }

        this.validator = rules.isEmpty() ? null : new DefaultPasswordValidator(rules);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || validator == null) {
            return true;
        }

        ValidationResult result = validator.validate(new PasswordData(password));

        if (result.isValid()) {
            return true;
        }

        List<String> errorMessages = new ArrayList<>();
        for (RuleResultDetail detail : result.getDetails()) {
            switch (detail.getErrorCode()) {
                case "TOO_SHORT":
                    errorMessages.add("Tamanho mínimo de " + minSize + " caracteres");
                    break;
                case "TOO_LONG":
                    errorMessages.add("Tamanho máximo de " + maxSize + " caracteres");
                    break;
                case "INSUFFICIENT_UPPERCASE":
                    errorMessages.add("Ao menos " + uppercase + " caractere maiúsculo");
                    break;
                case "INSUFFICIENT_LOWERCASE":
                    errorMessages.add("Ao menos " + lowercase + " caractere minúsculo");
                    break;
                case "INSUFFICIENT_DIGIT":
                    errorMessages.add("Ao menos " + digits + " dígito");
                    break;
                case "INSUFFICIENT_SPECIAL":
                    errorMessages.add("Ao menos " + special + " caractere especial");
                    break;
                case "INSUFFICIENT_ALPHABETICAL":
                    errorMessages.add("Ao menos " + letters + " letra");
                    break;
                case "ILLEGAL_WORD":
                    errorMessages.add("Não pode ser uma senha proibida ou comum");
                    break;
                case "ILLEGAL_SEQUENCE":
                    errorMessages.add("Não são permitidas sequências consecutivas ou de teclado");
                    break;
                default:
                    errorMessages.add("A senha não atende aos critérios políticos de segurança");
                    break;
            }
        }

        String mensagemFinal = String.join("; ", errorMessages);

        context.disableDefaultConstraintViolation();

        String template = (annotationMessage == null || annotationMessage.isEmpty())
                ? mensagemFinal
                : annotationMessage;

        context.buildConstraintViolationWithTemplate(template).addConstraintViolation();

        return false;
    }
}