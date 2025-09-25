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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTests {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card testCard;
    private CardCreateDto testCardCreateDto;
    private CardUpdateDto testCardUpdateDto;
    private CardResponseDto testCardResponseDto;

    @BeforeEach
    void setUp() {
        testCard = createTestCard(1L, "1234567812345678", "John Doe", LocalDate.of(2025, 12, 31));
        testCardCreateDto = createTestCardCreateDto("1234567812345678", "John Doe", LocalDate.of(2025, 12, 31));
        testCardUpdateDto = createTestCardUpdateDto("8765432187654321", "Jane Smith", LocalDate.of(2026, 6, 30));
        testCardResponseDto = createTestCardResponseDto(1L, "1234567812345678", "John Doe", LocalDate.of(2025, 12, 31));
    }

    @Test
    void createCard_WhenValidCardCreateDtoAndUserExists_ThenReturnsCardResponseDto() {
        Long userId = 1L;
        testCardCreateDto = new CardCreateDto("1234567812345678", userId, "John Doe", LocalDate.of(2025, 12, 31));
        User user = new User(userId, "John", "Doe", null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardMapper.toCard(testCardCreateDto)).thenReturn(testCard);
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardMapper.toCardResponseDto(testCard)).thenReturn(testCardResponseDto);

        CardResponseDto result = cardService.createCard(testCardCreateDto);

        assertThat(result).isEqualTo(testCardResponseDto);
        verify(userRepository).findById(userId);
        verify(cardMapper).toCard(testCardCreateDto);
        verify(cardRepository).save(testCard);
        verify(cardMapper).toCardResponseDto(testCard);
    }

    @Test
    void createCard_WhenUserNotFound_ThenThrowsResourceNotFoundException() {
        Long userId = 999L;
        testCardCreateDto = new CardCreateDto("1234567812345678", userId, "John Doe", LocalDate.of(2025, 12, 31));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(cardMapper.toCard(testCardCreateDto)).thenReturn(new Card());

        assertThatThrownBy(() -> cardService.createCard(testCardCreateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User by id: " + userId + " not found");

        verify(cardMapper).toCard(testCardCreateDto);
        verify(userRepository).findById(userId);
    }

    @Test
    void createCard_WhenRepositoryThrowsException_ThenPropagatesException() {
        Long userId = 1L;
        testCardCreateDto = new CardCreateDto("1234567812345678", userId, "John Doe", LocalDate.of(2025, 12, 31));
        User user = new User(userId, "John", "Doe", null, null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardMapper.toCard(testCardCreateDto)).thenReturn(testCard);
        when(cardRepository.save(testCard)).thenThrow(new RuntimeException("Database constraint violation"));

        assertThatThrownBy(() -> cardService.createCard(testCardCreateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database constraint violation");

        verify(userRepository).findById(userId);
        verify(cardMapper).toCard(testCardCreateDto);
        verify(cardRepository).save(testCard);
        verifyNoMoreInteractions(cardMapper);
    }

    @Test
    void getCardById_ShouldReturnCardResponseDto_WhenCardExists() {
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
        when(cardMapper.toCardResponseDto(testCard)).thenReturn(testCardResponseDto);

        CardResponseDto result = cardService.getCardById(cardId);

        assertThat(result).isEqualTo(testCardResponseDto);
        verify(cardRepository).findById(cardId);
        verify(cardMapper).toCardResponseDto(testCard);
    }

    @Test
    void getCardById_ShouldThrowException_WhenCardNotFound() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(cardId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Card by id " + cardId + " not found");
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, Long.MAX_VALUE})
    void getCardById_ShouldThrowException_WhenInvalidIds(Long cardId) {
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(cardId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Card by id " + cardId + " not found");
    }

    @Test
    void getCardsByIds_ShouldReturnList_WhenValidIds() {
        List<Long> cardIds = Arrays.asList(1L, 2L, 3L);
        Card secondCard = createTestCard(2L, "9876543298765432", "Alice Johnson", LocalDate.of(2027, 3, 15));
        Card thirdCard = createTestCard(3L, "5555444433332222", "Bob Wilson", LocalDate.of(2026, 11, 30));

        List<Card> cards = Arrays.asList(testCard, secondCard, thirdCard);
        List<CardResponseDto> expectedResponse = Arrays.asList(
                testCardResponseDto,
                createTestCardResponseDto(2L, "9876543298765432", "Alice Johnson", LocalDate.of(2027, 3, 15)),
                createTestCardResponseDto(3L, "5555444433332222", "Bob Wilson", LocalDate.of(2026, 11, 30))
        );

        when(cardRepository.findAllById(cardIds)).thenReturn(cards);
        when(cardMapper.toCardResponseDto(cards)).thenReturn(expectedResponse);

        List<CardResponseDto> result = cardService.getCardsByIds(cardIds);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getCardsByIds_ShouldReturnEmptyList_WhenIdsEmpty() {
        List<Long> emptyIds = Collections.emptyList();
        when(cardRepository.findAllById(emptyIds)).thenReturn(Collections.emptyList());
        when(cardMapper.toCardResponseDto(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<CardResponseDto> result = cardService.getCardsByIds(emptyIds);

        assertThat(result).isEmpty();
    }

    @Test
    void getCardsByIds_ShouldReturnOnlyFoundCards_WhenSomeIdsMissing() {
        List<Long> cardIds = Arrays.asList(1L, 100L, 2L);
        when(cardRepository.findAllById(cardIds)).thenReturn(List.of(testCard));
        when(cardMapper.toCardResponseDto(List.of(testCard))).thenReturn(List.of(testCardResponseDto));

        List<CardResponseDto> result = cardService.getCardsByIds(cardIds);

        assertThat(result).containsExactly(testCardResponseDto);
    }

    @Test
    void getCardsByIds_ShouldThrowException_WhenIdsNull() {
        when(cardRepository.findAllById(null)).thenThrow(new IllegalArgumentException("IDs cannot be null"));

        assertThatThrownBy(() -> cardService.getCardsByIds(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateCard_ShouldReturnUpdatedCard_WhenValidUpdate() {
        Long cardId = 1L;
        Card updatedCard = createTestCard(cardId, "8765432187654321", "Jane Smith", LocalDate.of(2026, 6, 30));
        CardResponseDto updatedDto = createTestCardResponseDto(cardId, "8765432187654321", "Jane Smith", LocalDate.of(2026, 6, 30));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);
        when(cardMapper.toCardResponseDto(any(Card.class))).thenReturn(updatedDto);

        CardResponseDto result = cardService.updateCard(cardId, testCardUpdateDto);

        assertThat(result).isEqualTo(updatedDto);
        verify(cardRepository).save(argThat(card ->
                "8765432187654321".equals(card.getNumber()) &&
                        "Jane Smith".equals(card.getHolder())
        ));
    }

    @ParameterizedTest
    @MethodSource("updateCardPartialUpdateProvider")
    void updateCard_ShouldUpdatePartialFields(CardUpdateDto partialUpdate) {
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toCardResponseDto(any(Card.class))).thenReturn(testCardResponseDto);

        CardResponseDto result = cardService.updateCard(cardId, partialUpdate);

        assertThat(result).isNotNull();
    }

    @Test
    void updateCard_ShouldThrowException_WhenCardNotFound() {
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateCard(cardId, testCardUpdateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Card by id " + cardId + " not found");
    }

    @Test
    void updateCard_ShouldHandleAllFieldsNull() {
        Long cardId = 1L;
        CardUpdateDto allNull = createTestCardUpdateDto(null, null, null);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toCardResponseDto(any(Card.class))).thenReturn(testCardResponseDto);

        CardResponseDto result = cardService.updateCard(cardId, allNull);

        assertThat(result).isNotNull();
        verify(cardRepository).save(argThat(card ->
                "1234567812345678".equals(card.getNumber()) &&
                        "John Doe".equals(card.getHolder())
        ));
    }

    @Test
    void updateCard_ShouldThrowException_WhenRepositoryFails() {
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> cardService.updateCard(cardId, testCardUpdateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }

    @Test
    void deleteCardById_ShouldCallRepositoryDelete() {
        Long cardId = 1L;
        doNothing().when(cardRepository).deleteById(cardId);

        cardService.deleteCardById(cardId);

        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void deleteCardById_ShouldThrowException_WhenRepositoryFails() {
        Long cardId = 1L;
        doThrow(new RuntimeException("Database error")).when(cardRepository).deleteById(cardId);

        assertThatThrownBy(() -> cardService.deleteCardById(cardId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 999L, Long.MAX_VALUE})
    void deleteCardById_ShouldCallRepositoryForVariousIds(Long cardId) {
        doNothing().when(cardRepository).deleteById(cardId);

        cardService.deleteCardById(cardId);

        verify(cardRepository).deleteById(cardId);
    }

    @Test
    void deleteCardById_ShouldThrowException_WhenIdNull() {
        doThrow(new IllegalArgumentException("ID cannot be null")).when(cardRepository).deleteById(null);

        assertThatThrownBy(() -> cardService.deleteCardById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> updateCardPartialUpdateProvider() {
        return Stream.of(
                Arguments.of(new CardUpdateDto("1111222233334444", null, null)),
                Arguments.of(new CardUpdateDto(null, "Updated Holder", null)),
                Arguments.of(new CardUpdateDto(null, null, LocalDate.of(2030, 12, 31))),
                Arguments.of(new CardUpdateDto("1111222233334444", "Updated Holder", null)),
                Arguments.of(new CardUpdateDto("1111222233334444", null, LocalDate.of(2030, 12, 31))),
                Arguments.of(new CardUpdateDto(null, "Updated Holder", LocalDate.of(2030, 12, 31))),
                Arguments.of(new CardUpdateDto("", "Updated Holder", LocalDate.of(2030, 12, 31)))
        );
    }

    private Card createTestCard(Long id, String number, String holder, LocalDate expirationDate) {
        Card card = new Card();
        card.setId(id);
        card.setNumber(number);
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);
        return card;
    }

    private CardCreateDto createTestCardCreateDto(String number, String holder, LocalDate expirationDate) {
        return new CardCreateDto(number, null, holder, expirationDate);
    }

    private CardUpdateDto createTestCardUpdateDto(String number, String holder, LocalDate expirationDate) {
        return new CardUpdateDto(number, holder, expirationDate);
    }

    private CardResponseDto createTestCardResponseDto(Long id, String number, String holder, LocalDate expirationDate) {
        return new CardResponseDto(id, null, number, holder, expirationDate);
    }
}
