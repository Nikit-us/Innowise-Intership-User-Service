package com.innowise.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CardUpdateDto(
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String number,

        @Size(max = 100)
        String holder,

        @Future
        LocalDate expirationDate
) {
}
