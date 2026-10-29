package com.example.trabalho.clinicaveterinaria.core.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.example.trabalho.clinicaveterinaria.core.validation.NotNullIfAnotherFieldHasValue;
import com.example.trabalho.clinicaveterinaria.core.validation.util.ReflectUtil;

public class NotNullIfAnotherFieldHasValueValidator implements ConstraintValidator<NotNullIfAnotherFieldHasValue, Object> {

    private String fieldName;
    private String expectedFieldValue;
    private String dependFieldName;
    private String message;

    @Override
    public void initialize(final NotNullIfAnotherFieldHasValue annotation) {
        this.fieldName = annotation.fieldName();
        this.expectedFieldValue = annotation.fieldValue();
        this.dependFieldName = annotation.dependFieldName();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(final Object rootObject, final ConstraintValidatorContext ctx) {
        if (rootObject == null) {
            return true;
        }

        Object fieldValue = ReflectUtil.extrairValor(rootObject, this.fieldName);
        Object dependFieldValue = ReflectUtil.extrairValor(rootObject, this.dependFieldName);

        boolean matchesCondition = (fieldValue != null && this.expectedFieldValue.equals(fieldValue.toString()))
                || (fieldValue == null && this.expectedFieldValue.isEmpty());

        boolean isDependValueEmpty = dependFieldValue == null
                || (dependFieldValue instanceof String str && str.isBlank());

        if (matchesCondition && isDependValueEmpty) {
            ctx.disableDefaultConstraintViolation();
            String template = (message != null && !message.isBlank())
                    ? message
                    : "Campo de preenchimento obrigatório";
            ctx.buildConstraintViolationWithTemplate(template)
               .addPropertyNode(dependFieldName)
               .addConstraintViolation();
            return false;
        }
        return true;
    }
}
