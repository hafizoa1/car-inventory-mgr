package com.ovah.inventoryservice.validator;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationError <T>{
    private String field;
    private String message;
    private ErrorCode errorCode;
    private T rejectedValue;

    public enum ErrorCode {
        INVALID_FORMAT,
        REQUIRED_FIELD,
        OUT_OF_RANGE, BUSINESS_RULE_VIOLATION,
    }
}
