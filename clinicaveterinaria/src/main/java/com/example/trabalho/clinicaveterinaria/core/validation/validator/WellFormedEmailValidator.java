package com.example.trabalho.clinicaveterinaria.core.validation.validator;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.example.trabalho.clinicaveterinaria.core.validation.WellFormedEmail;

public class WellFormedEmailValidator implements ConstraintValidator<WellFormedEmail, String> {

	private static final Logger logger = LoggerFactory.getLogger(WellFormedEmailValidator.class);

	private static final EmailValidator validator = EmailValidator.getInstance();

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		if (email != null && !email.isBlank()) {
			boolean result = validator.isValid(email);
			if (logger.isDebugEnabled()) {
				logger.debug("O e-mail {} foi considerado {}", maskEmail(email), result ? "válido" : "inválido");
			}
			return result;
		}
		return true;
	}

	private String maskEmail(String email) {
		int atIndex = email.indexOf('@');
		if (atIndex <= 1) {
			return "***@" + (atIndex >= 0 ? email.substring(atIndex + 1) : "");
		}
		return email.charAt(0) + "***" + email.substring(atIndex);
	}
}