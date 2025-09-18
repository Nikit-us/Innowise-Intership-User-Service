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
    public void createUser(UserCreateDto userCreateDto) {
        userRepository.save(userMapper.toUser(userCreateDto));
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User by id: " + id + " not found"));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User by email: " + email + " not found"));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getUsersById(List<Long> ids) {
        List<User> users = userRepository.findAllByIdIn(ids);
        return userMapper.toUserResponseDto(users);
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateDto userUpdateDto) {
        userRepository.updateUser(id, userUpdateDto.name(), userUpdateDto.surname(), userUpdateDto.birthDate()  ,userUpdateDto.email());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
