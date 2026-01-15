package com.amalitech.smartecommerce.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for Jakarta Bean Validation.
 * Validates DTOs using annotations like @NotBlank, @Email, @Size.
 */
public class ValidationUtil {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    private ValidationUtil() {}

    /**
     * Validate an object and return all validation error messages.
     * @param obj The object to validate
     * @return Set of error messages (empty if valid)
     */
    public static <T> Set<String> validate(T obj) {
        Set<ConstraintViolation<T>> violations = validator.validate(obj);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }


}
