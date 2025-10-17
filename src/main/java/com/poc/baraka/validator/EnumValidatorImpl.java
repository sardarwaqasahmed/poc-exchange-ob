package com.poc.baraka.validator;

/**
 * @author Waqas Ahmed
 */
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, String> {

    private String[] acceptedValues;

    @Override
    public void initialize(EnumValidator annotation) {
        acceptedValues = Arrays.stream(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false; // treat null as invalid
        return Arrays.asList(acceptedValues).contains(value.toUpperCase()); // optional case-insensitive
    }
}
