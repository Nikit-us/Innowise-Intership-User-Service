package com.innowise.service;

import com.innowise.dto.card.CardCreateDto;
import com.innowise.dto.card.CardResponseDto;
import com.innowise.dto.card.CardUpdateDto;
import com.innowise.model.Card;

import java.util.List;

public interface CardService {
    CardResponseDto createCard(CardCreateDto cardCreateDto);

    CardResponseDto getCardById(Long id);
    List<CardResponseDto> getCardsByIds(List<Long> ids);

    CardResponseDto updateCard(Long id, CardUpdateDto cardUpdateDto);

    void deleteCardById(Long id);
}
