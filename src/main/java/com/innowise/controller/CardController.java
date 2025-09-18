package com.innowise.controller;

import com.innowise.dto.card.CardCreateDto;
import com.innowise.dto.card.CardResponseDto;
import com.innowise.dto.card.CardUpdateDto;
import com.innowise.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDto> getCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping
    public ResponseEntity<List<CardResponseDto>> geCardsByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(cardService.getCardsByIds(ids));
    }

    @PostMapping
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CardCreateDto cardCreateDto) {
        return ResponseEntity.ok(cardService.createCard(cardCreateDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponseDto> updateCard(@PathVariable Long id, @Valid @RequestBody CardUpdateDto cardUpdateDto) {
        return ResponseEntity.ok(cardService.updateCard(id, cardUpdateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCardById(id);
        return ResponseEntity.noContent().build();
    }
}
