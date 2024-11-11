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
        doValidate(request);
        returnErrors();

        if (next != null) {
            errorAggregator.merge(next.validate(request));
        }

        return errorAggregator;
    }

    protected abstract void doValidate(T request) throws ValidationException;

    protected void addError(String field, String message, ValidationError.ErrorCode code, T rejectedValue) {
        errorAggregator.addError(new ValidationError<>(field, message, code, rejectedValue));
    }

    protected void returnErrors() {
        errorAggregator.throwIfHasErrors();
    }

    protected void clearErrors() {
        errorAggregator.clear();
    }
}