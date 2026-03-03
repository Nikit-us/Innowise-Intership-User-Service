package com.innowise.dto.card;

import java.time.LocalDate;
import java.util.UUID;

public record CardResponseDto(
        Long id,
        UUID userId,
        String maskOfNumber,
        String holder,
        LocalDate expirationDate
) {
}
