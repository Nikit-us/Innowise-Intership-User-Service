package com.innowise.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CardUpdateDto(
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String number,

        @Size(max = 100, message = "Card holder name must not exceed 100 characters")
        String holder,

        @Future(message = "Expiration date must be in the future")
        LocalDate expirationDate
) {
}
