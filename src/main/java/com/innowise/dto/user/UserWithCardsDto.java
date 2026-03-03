package com.innowise.dto.user;

import com.innowise.dto.card.CardResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserWithCardsDto(
        UUID id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        List<CardResponseDto> cards
) {
}
