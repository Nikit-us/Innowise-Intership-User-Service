package com.innowise.service;

import com.innowise.dto.user.UserCreateDto;
import com.innowise.dto.user.UserResponseDto;
import com.innowise.dto.user.UserUpdateDto;
import com.innowise.dto.user.UserWithCardsDto;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserCreateDto userCreateDto);

    UserWithCardsDto getUserById(Long id);
    UserResponseDto getUserByEmail(String email);
    List<UserResponseDto> getUsersByIds(List<Long> ids);

    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);

    void deleteUser(Long id);
}
