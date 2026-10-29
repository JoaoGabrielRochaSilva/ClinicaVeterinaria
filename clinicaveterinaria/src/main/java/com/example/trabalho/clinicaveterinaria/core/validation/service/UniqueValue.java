package com.example.trabalho.clinicaveterinaria.core.validation.service;

public interface UniqueValue {
    boolean isValueUnique(Object value, String attributeName, Object rootObject);
}
