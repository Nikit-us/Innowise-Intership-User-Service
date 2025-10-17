package com.innowise.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String code,
        String message,
        List<ValidationError> errors
) {
    public ErrorResponse(int status, String code, String message) {
        this(status, code, message, null);
    }

    public record ValidationError(
            String field,
            String defaultMessage
    ){}
}
