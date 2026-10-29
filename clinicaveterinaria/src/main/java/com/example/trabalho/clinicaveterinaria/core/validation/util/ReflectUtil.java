package com.example.trabalho.clinicaveterinaria.core.validation.util;


import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.PropertyAccessorFactory;

public class ReflectUtil {


	public static Object extrairValor(Object object, String propertyName) {
		if (object == null)
			return null;
		try {
			// Suporte a Records nativos
			if (object.getClass().isRecord()) {
				return object.getClass().getMethod(propertyName).invoke(object);
			}
			// Suporte a Classes legadas com Getters
			return PropertyAccessorFactory.forBeanPropertyAccess(object).getPropertyValue(propertyName);
		} catch (Exception e) {
			// Fallback para atributo privado
			return new DirectFieldAccessor(object).getPropertyValue(propertyName);
		}
	}

}
