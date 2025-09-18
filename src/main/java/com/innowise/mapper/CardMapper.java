package com.innowise.mapper;

import com.innowise.dto.card.CardCreateDto;
import com.innowise.dto.card.CardResponseDto;
import com.innowise.dto.card.CardUpdateDto;
import com.innowise.model.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
    @Mapping(target = "id",  ignore = true)
    @Mapping(target = "user",   ignore = true)
    Card toCard(CardCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "number", ignore = true)
    Card toCard(CardUpdateDto dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "maskOfNumber", source = "number", qualifiedByName = "maskedNumber")
    CardResponseDto toCardResponseDto(Card card);

    List<CardResponseDto> toCardResponseDto(List<Card> cards);

    @Named("maskedNumber")
    default String maskedNumber(String number) {
        if(number == null || number.length() != 16) {
            return null;
        }
        return "**** **** **** " + number.substring(12);
    }
}
