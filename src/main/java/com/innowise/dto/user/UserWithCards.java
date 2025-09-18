package com.innowise.dto.user;

import com.innowise.dto.card.CardResponseDto;

import java.time.LocalDate;
import java.util.List;

public record UserWithCards(
        Long id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        List<CardResponseDto> cards
) {
}
