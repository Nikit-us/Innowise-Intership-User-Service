package com.innowise.service;

import com.innowise.dto.user.UserCreateDto;
import com.innowise.dto.user.UserResponseDto;
import com.innowise.dto.user.UserUpdateDto;
import com.innowise.dto.user.UserWithCardsDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    void createUser(UUID id, UserCreateDto userCreateDto);

    UserWithCardsDto getUserById(UUID id);
    UserResponseDto getUserByEmail(String email);
    List<UserResponseDto> getUsersByIds(List<UUID> ids);

    void updateUser(UUID id, UserUpdateDto userUpdateDto);

    void deleteUser(UUID id);
}
