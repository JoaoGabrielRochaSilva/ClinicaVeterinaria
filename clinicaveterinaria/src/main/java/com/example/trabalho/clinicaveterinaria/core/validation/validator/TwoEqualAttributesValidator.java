package com.example.trabalho.clinicaveterinaria.core.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.example.trabalho.clinicaveterinaria.core.validation.TwoEqualAttributes;
import com.example.trabalho.clinicaveterinaria.core.validation.util.ReflectUtil;

public class TwoEqualAttributesValidator implements ConstraintValidator<TwoEqualAttributes, Object> {

    private String attribute1;
    private String attribute2;
    private String message;

    @Override
    public void initialize(final TwoEqualAttributes annotation) {
        this.attribute1 = annotation.attribute1();
        this.attribute2 = annotation.attribute2();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(final Object rootObject, final ConstraintValidatorContext ctx) {
        if (rootObject == null) {
            return true;
        }

        Object field1Value = ReflectUtil.extrairValor(rootObject, this.attribute1);
        Object field2Value = ReflectUtil.extrairValor(rootObject, this.attribute2);

        boolean valid = (field1Value == null && field2Value == null) ||
                (field1Value != null && field1Value.equals(field2Value));

        if (!valid) {
            ctx.disableDefaultConstraintViolation();
            String template = (message != null && !message.isBlank())
                    ? message
                    : "Os valores são diferentes";
            ctx.buildConstraintViolationWithTemplate(template)
                    .addPropertyNode(attribute2)
                    .addConstraintViolation();
        }
        return valid;
    }
}
