package com.innowise.dto.card;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CardCreateDto(
        @NotNull
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String number,

        @NotBlank
        Long userId,

        @NotBlank
        @Size(max = 100)
        String holder,

        @NotNull
        @Future
        LocalDate expirationDate
) {
}
