package com.ovah.inventoryservice.validator;

import com.ovah.inventoryservice.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationErrorAggregator<T> {
    private final List<ValidationError<T>> errors = new ArrayList<>();

    public ValidationErrorAggregator<T> addError(ValidationError<T> error) {
        errors.add(error);
        return this;
    }

    public ValidationErrorAggregator<T> addErrors(List<ValidationError<T>> newErrors) {
        errors.addAll(newErrors);
        return this;
    }

    public void throwIfHasErrors() {
        if (!errors.isEmpty()) {
            throw new ValidationException("Multiple validation errors occurred",
                    new ArrayList<>(errors));// Create new list to avoid exposing internal state
        }
        //clear();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ValidationError<T>> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void merge(ValidationErrorAggregator<T> other) {
        if (other != null && other.hasErrors()) {
            this.errors.addAll(other.getErrors());
        }
    }

    public int getErrorCount() {
        return errors.size();
    }

    public boolean hasErrorForField(String field) {
        return errors.stream()
                .anyMatch(error -> error.getField().equals(field));
    }

    public List<ValidationError<T>> getErrorsForField(String field) {
        return errors.stream()
                .filter(error -> error.getField().equals(field))
                .collect(Collectors.toUnmodifiableList());
    }

    public void clear() {
        errors.clear();
    }
}
