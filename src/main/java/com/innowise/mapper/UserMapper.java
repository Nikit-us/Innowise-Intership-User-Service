package com.innowise.mapper;

import com.innowise.dto.user.UserCreateDto;
import com.innowise.dto.user.UserResponseDto;
import com.innowise.dto.user.UserUpdateDto;
import com.innowise.dto.user.UserWithCardsDto;
import com.innowise.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CardMapper.class})
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    User toUser(UserCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    User toUser(UserUpdateDto dto);

    UserResponseDto toUserResponseDto(User user);
    List<UserResponseDto> toUserResponseDto(List<User> users);

    UserWithCardsDto toUserWithCards(User user);

    void merge(UserUpdateDto source, @MappingTarget User target);
}
