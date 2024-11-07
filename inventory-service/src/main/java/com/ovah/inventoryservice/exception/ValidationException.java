package com.ovah.inventoryservice.exception;

import com.ovah.inventoryservice.validator.ValidationError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Class is declared generic to hold a specific type of ValidationError
public class ValidationException extends RuntimeException {
    private final List<ValidationError<?>> errors;

    public ValidationException(String message, List<? extends ValidationError<?>> errors) {
        super(message);
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public ValidationException(ValidationError<?> error) {
        this("Validation failed", Collections.singletonList(error));
    }

    public List<ValidationError<?>> getErrors() {
        return errors;
    }

    // Utility methods for creating specific validation errors
    public static <T> ValidationException invalidField(String fieldName, String errorDetail, T rejectedValue) {
        ValidationError<T> error = new ValidationError<>(
                fieldName,
                errorDetail,
                ValidationError.ErrorCode.INVALID_FORMAT,
                rejectedValue
        );
        return new ValidationException(error);
    }

    public static <T> ValidationException requiredFieldMissing(String fieldName, T rejectedValue) {
        ValidationError<T> error = new ValidationError<>(
                fieldName,
                "Required field missing",
                ValidationError.ErrorCode.REQUIRED_FIELD,
                rejectedValue
        );
        return new ValidationException(error);
    }

    public static <T> ValidationException outOfRange(String fieldName, Number min, Number max, T rejectedValue) {
        ValidationError<T> error = new ValidationError<>(
                fieldName,
                String.format("Must be between %s and %s", min, max),
                ValidationError.ErrorCode.OUT_OF_RANGE,
                rejectedValue
        );
        return new ValidationException(error);
    }
}