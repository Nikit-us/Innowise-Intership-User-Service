package com.innowise.service;

import com.innowise.dto.card.CardCreateDto;
import com.innowise.dto.card.CardResponseDto;
import com.innowise.dto.card.CardUpdateDto;

import java.util.List;

public interface CardService {
    void createCard(CardCreateDto cardCreateDto);

    CardResponseDto getCardById(Long id);
    List<CardResponseDto> getCardsByIds(List<Long> ids);

    void updateCard(Long id,CardUpdateDto cardUpdateDto);

    void deleteCardById(Long id);
}
