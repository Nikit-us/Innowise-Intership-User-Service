package com.innowise.integration;

import com.innowise.AbstractIntegrationTest;
import com.innowise.dto.card.CardCreateDto;
import com.innowise.dto.card.CardResponseDto;
import com.innowise.dto.card.CardUpdateDto;
import com.innowise.model.Card;
import com.innowise.model.User;
import com.innowise.repository.CardRepository;
import com.innowise.repository.UserRepository;
import com.innowise.service.CardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CardServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(
                new User(null, "Test", "User", LocalDate.of(1995, 1, 1), "test.user@cards.com", null)
        );
    }

    @AfterEach
    void tearDown() {
        cardRepository.deleteAll();
        userRepository.deleteAll();
        cacheManager.getCache("cards").clear();
    }

    @Test
    void createCard_shouldSaveCardToDatabase() {
        CardCreateDto createDto = new CardCreateDto(
                "1111222233334444",
                testUser.getId(),
                "Test User",
                LocalDate.now().plusYears(2)
        );

        CardResponseDto responseDto = cardService.createCard(createDto);

        Optional<Card> savedCardOptional = cardRepository.findById(responseDto.id());
        assertTrue(savedCardOptional.isPresent());
        Card savedCard = savedCardOptional.get();

        assertAll(
                () -> assertNotNull(responseDto.id()),
                () -> assertEquals(createDto.number(), savedCard.getNumber()),
                () -> assertEquals(createDto.holder(), savedCard.getHolder()),
                () -> assertEquals(testUser.getId(), savedCard.getUser().getId())
        );
    }

    @Test
    void getCardById_shouldCacheCard() {
        Card savedCard = cardRepository.save(
                new Card(null, testUser, "5555666677778888", "Holder", LocalDate.now().plusYears(3))
        );
        Long cardId = savedCard.getId();

        CardResponseDto cardFromService = cardService.getCardById(cardId);
        CardResponseDto cachedCard = cacheManager.getCache("cards").get(cardId, CardResponseDto.class);

        assertAll(
                () -> assertNotNull(cardFromService),
                () -> assertEquals(cardId, cardFromService.id()),
                () -> assertNotNull(cachedCard),
                () -> assertEquals(cardId, cachedCard.id()),
                () -> assertEquals("**** **** **** 8888", cachedCard.maskOfNumber())
        );
    }

    @Test
    void updateCard_shouldUpdateCardInDatabaseAndCache() {
        Card savedCard = cardRepository.save(
                new Card(null, testUser, "9999000011112222", "Old Holder", LocalDate.now().plusYears(1))
        );
        Long cardId = savedCard.getId();

        cardService.getCardById(cardId);
        
        CardUpdateDto updateDto = new CardUpdateDto(null, "New Holder", null);
        cardService.updateCard(cardId, updateDto);

        Card updatedCardInDb = cardRepository.findById(cardId).orElseThrow();
        CardResponseDto updatedCardInCache = cacheManager.getCache("cards").get(cardId, CardResponseDto.class);

        assertAll(
                () -> assertEquals("New Holder", updatedCardInDb.getHolder()),
                () -> assertNotNull(updatedCardInCache),
                () -> assertEquals("New Holder", updatedCardInCache.holder())
        );
    }

    @Test
    void deleteCard_shouldRemoveCardFromDatabaseAndCache() {
        Card savedCard = cardRepository.save(
                new Card(null, testUser, "4444555566667777", "Temp Holder", LocalDate.now().plusYears(4))
        );
        Long cardId = savedCard.getId();

        cardService.getCardById(cardId);
        assertThat(cacheManager.getCache("cards").get(cardId)).isNotNull();
        
        cardService.deleteCardById(cardId);

        assertAll(
                () -> assertThat(cardRepository.findById(cardId)).isEmpty(),
                () -> assertThat(cacheManager.getCache("cards").get(cardId)).isNull()
        );
    }
}