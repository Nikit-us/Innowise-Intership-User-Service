package com.innowise.service.impl;

import com.innowise.dto.card.CardCreateDto;
import com.innowise.dto.card.CardResponseDto;
import com.innowise.dto.card.CardUpdateDto;
import com.innowise.exception.ResourceNotFoundException;
import com.innowise.mapper.CardMapper;
import com.innowise.mapper.UserMapper;
import com.innowise.model.Card;
import com.innowise.repository.CardRepository;
import com.innowise.service.CardService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void createCard(CardCreateDto cardCreateDto) {
        Card card = cardMapper.toCard(cardCreateDto);
        cardRepository.save(card);
    }

    @Override
    public CardResponseDto getCardById(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card by id " + id + " not found"));
        return cardMapper.toCardResponseDto(card);
    }

    @Override
    public List<CardResponseDto> getCardsByIds(List<Long> ids) {
        List<Card> cards = cardRepository.findAllByIds(ids);
        return cardMapper.toCardResponseDto(cards);
    }

    @Override
    @Transactional
    public void updateCard(Long id, CardUpdateDto cardUpdateDto) {
        cardRepository.updateCard(id, cardUpdateDto.number(), cardUpdateDto.holder(), cardUpdateDto.expirationDate());
    }

    @Override
    @Transactional
    public void deleteCardById(Long id) {
        cardRepository.deleteById(id);
    }
}
