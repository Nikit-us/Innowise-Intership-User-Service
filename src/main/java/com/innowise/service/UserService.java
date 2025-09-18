package com.innowise.service;

import com.innowise.dto.user.UserCreateDto;
import com.innowise.dto.user.UserResponseDto;
import com.innowise.dto.user.UserUpdateDto;

import java.util.List;

public interface UserService {
    void createUser(UserCreateDto userCreateDto);

    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByEmail(String email);
    List<UserResponseDto> getUsersById(List<Long> ids);

    void updateUser(Long id, UserUpdateDto userUpdateDto);

    void deleteUser(Long id);
}
