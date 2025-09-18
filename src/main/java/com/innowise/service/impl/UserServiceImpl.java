package com.innowise.service.impl;

import com.innowise.dto.user.UserCreateDto;
import com.innowise.dto.user.UserResponseDto;
import com.innowise.dto.user.UserUpdateDto;
import com.innowise.exception.ResourceNotFoundException;
import com.innowise.mapper.UserMapper;
import com.innowise.model.User;
import com.innowise.repository.UserRepository;
import com.innowise.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        return userMapper.toUserResponseDto(userRepository.save(userMapper.toUser(userCreateDto)));
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        return userMapper.toUserResponseDto(findUserById(id));
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User by email: " + email + " not found"));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getUsersByIds(List<Long> ids) {
        List<User> users = userRepository.findAllByIdIn(ids);
        return userMapper.toUserResponseDto(users);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        userRepository.updateUser(id, userUpdateDto.name(), userUpdateDto.surname(), userUpdateDto.birthDate()  ,userUpdateDto.email());
        return userMapper.toUserResponseDto(findUserById(id));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User by id: " + id + " not found"));
    }
}
