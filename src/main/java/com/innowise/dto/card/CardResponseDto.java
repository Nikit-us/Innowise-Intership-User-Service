package com.innowise.dto.card;

import java.time.LocalDate;

public record CardResponseDto(
        Long id,
        Long userId,
        String maskOfNumber,
        String holder,
        LocalDate expirationDate
) {
}
