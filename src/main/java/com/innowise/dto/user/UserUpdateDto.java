package com.innowise.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserUpdateDto(
        @Size(max = 100)
        String name,

        @Size(max = 100)
        String surname,

        @Past
        LocalDate birthDate,

        @Email
        String email
) {
}
