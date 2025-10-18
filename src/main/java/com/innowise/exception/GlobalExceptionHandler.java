package com.innowise.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final Pattern CONSTRAINT_PATTERN = Pattern.compile("constraint \\[([^\\]]+)\\]");
    private static final Pattern DETAIL_PATTERN = Pattern.compile("Detail: Key \\(([^)]+)\\)=\\(([^)]+)\\)");
    private static final String RESOURCE_ALREADY_EXISTS = "RESOURCE_ALREADY_EXISTS";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), "RESOURCE_NOT_FOUND", e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(), RESOURCE_ALREADY_EXISTS, e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(InvalidRequestException e) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "INVALID_REQUEST", e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("Database constraint violation: {}", e.getMessage());

        String message = e.getMessage();
        if (message == null) {
            return createGenericConstraintError();
        }

        String userFriendlyMessage = parseConstraintViolation(message);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                RESOURCE_ALREADY_EXISTS,
                userFriendlyMessage
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    private String parseConstraintViolation(String errorMessage) {
        if (errorMessage.contains("users_email_key")) {
            String email = extractValueFromDetail(errorMessage, "email");
            if (email != null) {
                return String.format("User already exists with email: %s", email);
            }
            return "User with this email already exists";
        }


        Matcher constraintMatcher = CONSTRAINT_PATTERN.matcher(errorMessage);
        if (constraintMatcher.find()) {
            String constraintName = constraintMatcher.group(1);
            String field = extractFieldFromConstraint(constraintName);
            return String.format("Resource with this %s already exists", field);
        }

        return "Resource already exists in the database";
    }

    private String extractValueFromDetail(String errorMessage, String fieldName) {
        Matcher detailMatcher = DETAIL_PATTERN.matcher(errorMessage);
        if (detailMatcher.find()) {
            String key = detailMatcher.group(1);
            String value = detailMatcher.group(2);

            if (key.equalsIgnoreCase(fieldName)) {
                return value;
            }
        }
        return null;
    }

    private String extractFieldFromConstraint(String constraintName) {
        if (constraintName.contains("_")) {
            String[] parts = constraintName.split("_");
            if (parts.length >= 2) {
                return parts[parts.length - 2];
            }
        }
        return "field";
    }

    private ResponseEntity<ErrorResponse> createGenericConstraintError() {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                RESOURCE_ALREADY_EXISTS,
                "Resource already exists in the database"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ErrorResponse.ValidationError> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorResponse.ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Validation failed for one or more fields.",
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Authentication failed or token is invalid."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                "You do not have the required permissions for this resource."
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception e) {
        log.error("Unexpected error occurred", e);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}