package com.innowise.dto;

public record ValidateTokenResponse(
        boolean valid,
        Long userId,
        String tokenType
) {
}
