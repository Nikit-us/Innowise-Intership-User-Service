package com.innowise.service.impl;

import com.innowise.dto.card.CardCreateDto;
import com.innowise.dto.card.CardResponseDto;
import com.innowise.dto.card.CardUpdateDto;
import com.innowise.exception.ResourceNotFoundException;
import com.innowise.mapper.CardMapper;
import com.innowise.model.Card;
import com.innowise.repository.CardRepository;
import com.innowise.service.CardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Override
    @Transactional
    public CardResponseDto createCard(CardCreateDto cardCreateDto) {
        return cardMapper.toCardResponseDto(cardRepository.save(cardMapper.toCard(cardCreateDto)));
    }

    @Override
    public CardResponseDto getCardById(Long id) {
        return cardMapper.toCardResponseDto(findCardById(id));
    }

    @Override
    public List<CardResponseDto> getCardsByIds(List<Long> ids) {
        List<Card> cards = cardRepository.findAllByIds(ids);
        return cardMapper.toCardResponseDto(cards);
    }

    @Override
    @Transactional
    public CardResponseDto updateCard(Long id, CardUpdateDto cardUpdateDto) {
        cardRepository.updateCard(id, cardUpdateDto.number(), cardUpdateDto.holder(), cardUpdateDto.expirationDate());
        return cardMapper.toCardResponseDto(findCardById(id));
    }

    @Override
    @Transactional
    public void deleteCardById(Long id) {
        cardRepository.deleteById(id);
    }

    private Card findCardById(Long id) {
        return cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card by id " + id + " not found"));
    }
}
