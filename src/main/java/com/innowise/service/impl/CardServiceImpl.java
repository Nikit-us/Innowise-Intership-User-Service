package com.innowise.service.impl;

import com.innowise.dto.card.CardCreateDto;
import com.innowise.dto.card.CardResponseDto;
import com.innowise.dto.card.CardUpdateDto;
import com.innowise.exception.ResourceNotFoundException;
import com.innowise.mapper.CardMapper;
import com.innowise.model.Card;
import com.innowise.model.User;
import com.innowise.repository.CardRepository;
import com.innowise.repository.UserRepository;
import com.innowise.service.CardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public CardResponseDto createCard(CardCreateDto cardCreateDto) {
        Card card = cardMapper.toCard(cardCreateDto);
        User user = userRepository.findById(cardCreateDto.userId()).orElseThrow(() -> new ResourceNotFoundException("User by id: " + cardCreateDto.userId() + " not found"));
        card.setUser(user);
        updateUserWithCardsCache(user.getId());
        return cardMapper.toCardResponseDto(cardRepository.save(card));
    }

    @Override
    @Cacheable(value = "cards", key = "#id")
    public CardResponseDto getCardById(Long id) {
        return cardMapper.toCardResponseDto(findCardById(id));
    }

    @Override
    public List<CardResponseDto> getCardsByIds(List<Long> ids) {
        List<Card> cards = cardRepository.findAllById(ids);
        return cardMapper.toCardResponseDto(cards);
    }

    public boolean isOwner(Long cardId, UUID userId) {
        return cardRepository.findOwnerIdById(cardId)
                .map(owner -> owner.getId().equals(userId))
                .orElse(false);
    }

    @Override
    @Transactional
    @CachePut(value = "cards", key = "#id")
    public CardResponseDto updateCard(Long id, CardUpdateDto cardUpdateDto) {
        Card card = findCardById(id);
        if(cardUpdateDto.holder() != null) {
            card.setHolder(cardUpdateDto.holder());
        }
        if (cardUpdateDto.expirationDate() != null) {
            card.setExpirationDate(cardUpdateDto.expirationDate());
        }
        if (cardUpdateDto.number() != null) {
            card.setNumber(cardUpdateDto.number());
        }
        return cardMapper.toCardResponseDto(cardRepository.save(card));
    }

    @Override
    @Transactional
    @CacheEvict(value = "cards", key = "#id")
    public void deleteCardById(Long id) {
        cardRepository.deleteById(id);
    }

    private Card findCardById(Long id) {
        return cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card by id " + id + " not found"));
    }

    private void updateUserWithCardsCache(UUID userId) {
        Cache cache = cacheManager.getCache("usersWithCards");
        if (cache != null) {
            cache.evict(userId);
        }
    }
}