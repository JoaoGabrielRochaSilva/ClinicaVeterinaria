package com.example.trabalho.clinicaveterinaria.core.validation.validator;

import java.math.BigDecimal;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.example.trabalho.clinicaveterinaria.core.validation.NumericAttributesRelation;
import com.example.trabalho.clinicaveterinaria.core.validation.util.ReflectUtil;
import com.example.trabalho.clinicaveterinaria.core.validation.AttributesRelations;

public class NumericAttributesRelationValidator implements ConstraintValidator<NumericAttributesRelation, Object> {

    private String attribute1;
    private String attribute2;
    private AttributesRelations relation;
    private double epsilon;
    private String message;

    @Override
    public void initialize(final NumericAttributesRelation annotation) {
        this.attribute1 = annotation.attribute1();
        this.attribute2 = annotation.attribute2();
        this.relation = annotation.relation();
        this.epsilon = annotation.epsilon();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(final Object rootObject, final ConstraintValidatorContext ctx) {
        if (rootObject == null) {
            return true;
        }

        Object field1Value = ReflectUtil.extrairValor(rootObject, this.attribute1);
        Object field2Value = ReflectUtil.extrairValor(rootObject, this.attribute2);

        if (field1Value == null || field2Value == null) {
            return true; 
        }

        boolean valid = false;
        
        if (field1Value instanceof Number && field2Value instanceof Number) {
            BigDecimal num1 = new BigDecimal(field1Value.toString());
            BigDecimal num2 = new BigDecimal(field2Value.toString());
            BigDecimal diff = num1.subtract(num2);
            BigDecimal eps = BigDecimal.valueOf(Math.abs(this.epsilon));
            
            valid = switch (relation) {
                case EQUAL -> diff.abs().compareTo(eps) <= 0;
                case DIFFERENT -> diff.abs().compareTo(eps) > 0;
                case GREATERTHAN -> diff.compareTo(eps) > 0;
                case GREATEROREQUAL -> diff.compareTo(eps.negate()) >= 0;
                case LESSTHAN -> diff.compareTo(eps.negate()) < 0;
                case LESSOREQUAL -> diff.compareTo(eps) <= 0;
            };
        }

        if (!valid) {
            ctx.disableDefaultConstraintViolation();
            String template = (message != null && !message.isBlank())
                    ? message
                    : "A relação numérica entre os atributos é inválida";
            ctx.buildConstraintViolationWithTemplate(template)
               .addPropertyNode(attribute2)
               .addConstraintViolation();
        }

        return valid;
    }
}
