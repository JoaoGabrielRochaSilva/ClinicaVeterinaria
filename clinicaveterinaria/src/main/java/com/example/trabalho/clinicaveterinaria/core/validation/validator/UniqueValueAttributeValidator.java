package com.example.trabalho.clinicaveterinaria.core.validation.validator;

import org.springframework.context.ApplicationContext;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.example.trabalho.clinicaveterinaria.core.validation.UniqueValueAttribute;
import com.example.trabalho.clinicaveterinaria.core.validation.service.UniqueValue;
import com.example.trabalho.clinicaveterinaria.core.validation.util.ReflectUtil;

public class UniqueValueAttributeValidator implements ConstraintValidator<UniqueValueAttribute, Object> {

    private String attribute;
    private String message;
    private final ApplicationContext applicationContext;
    private UniqueValue service;

    public UniqueValueAttributeValidator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(final UniqueValueAttribute annotation) {
        this.attribute = annotation.attribute();
        this.message = annotation.message();

        Class<? extends UniqueValue> clazz = annotation.service();
        String serviceQualifier = annotation.serviceQualifier();

        if (serviceQualifier != null && !serviceQualifier.isBlank()) {
            this.service = applicationContext.getBean(serviceQualifier, clazz);
        } else {
            this.service = applicationContext.getBean(clazz);
        }
    }

    @Override
    public boolean isValid(final Object rootObject, final ConstraintValidatorContext ctx) {
        if (rootObject == null) {
            return true;
        }

        Object valorExtraido = ReflectUtil.extrairValor(rootObject, this.attribute);

        if (valorExtraido == null || (valorExtraido instanceof String str && str.isBlank())) {
            return true;
        }

        boolean valid = service.isValueUnique(valorExtraido, this.attribute, rootObject);

        if (!valid) {
            ctx.disableDefaultConstraintViolation();
            String template = (message != null && !message.isBlank())
                    ? message
                    : "O valor informado já está em uso";
            ctx.buildConstraintViolationWithTemplate(template)
                    .addPropertyNode(attribute)
                    .addConstraintViolation();
        }
        return valid;
    }
}
