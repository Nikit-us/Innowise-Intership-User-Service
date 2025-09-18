package com.innowise.exception;

import java.util.List;

public record ErrorResponse(
        int status,
        String code,
        String message,
        List<ValidationError> errors
) {
    public record ValidationError(
            String field,
            String defaultMessage
    ){}
}
