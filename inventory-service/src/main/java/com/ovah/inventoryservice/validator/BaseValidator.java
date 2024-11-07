package com.ovah.inventoryservice.validator;

import com.ovah.inventoryservice.exception.ValidationException;

import java.util.List;

public abstract class BaseValidator<T> {
    protected BaseValidator<T> next;
    protected ValidationErrorAggregator<T> errorAggregator;

    public BaseValidator() {
        this.errorAggregator = new ValidationErrorAggregator<>();
    }

    public BaseValidator<T> setNext(BaseValidator<T> next) {
        this.next = next;
        return next;  // Return next to allow chaining
    }

    public final ValidationErrorAggregator<T> validate(T request) {
        try {
            doValidate(request);
        } catch (ValidationException e) {
            // Safe to cast because we know these errors are for type T
            @SuppressWarnings("unchecked")
            List<ValidationError<T>> typedErrors = (List<ValidationError<T>>) (List<?>) e.getErrors();
            errorAggregator.addErrors(typedErrors);
        }

        if (next != null) {
            errorAggregator.merge(next.validate(request));
        }

        return errorAggregator;
    }

    protected abstract void doValidate(T request) throws ValidationException;

    protected void addError(String field, String message, ValidationError.ErrorCode code, T rejectedValue) {
        errorAggregator.addError(new ValidationError<>(field, message, code, rejectedValue));
    }
}