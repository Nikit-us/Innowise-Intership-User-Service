package com.innowise.service.impl;

import com.innowise.dto.user.UserCreateDto;
import com.innowise.dto.user.UserResponseDto;
import com.innowise.dto.user.UserUpdateDto;
import com.innowise.dto.user.UserWithCardsDto;
import com.innowise.exception.ResourceNotFoundException;
import com.innowise.mapper.UserMapper;
import com.innowise.model.User;
import com.innowise.repository.UserRepository;
import com.innowise.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        return userMapper.toUserResponseDto(
                userRepository.save(userMapper.toUser(userCreateDto))
        );
    }

    @Override
    @Cacheable(value = "usersWithCards", key = "#id")
    @Transactional(readOnly = true)
    public UserWithCardsDto getUserById(Long id) {
        return userMapper.toUserWithCards(findUserById(id));
    }

    @Override
    @Cacheable(value = "usersInfo", key = "#email")
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User by email: " + email + " not found"));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getUsersByIds(List<Long> ids) {
        List<User> users = userRepository.findAllById(ids);
        return userMapper.toUserResponseDto(users);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = findUserById(id);
        String oldEmail = user.getEmail();

        if (userUpdateDto.name() != null) {
            user.setName(userUpdateDto.name());
        }
        if (userUpdateDto.surname() != null) {
            user.setSurname(userUpdateDto.surname());
        }
        if (userUpdateDto.birthDate() != null) {
            user.setBirthDate(userUpdateDto.birthDate());
        }
        if (userUpdateDto.email() != null) {
            user.setEmail(userUpdateDto.email());
        }

        User updatedUser = userRepository.save(user);

        updateUsersCache(updatedUser, oldEmail);

        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        deleteUserCache(id, findUserById(id).getEmail());
        userRepository.deleteById(id);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User by id: " + id + " not found"));
    }

    private void updateUsersCache(User updatedUser, String oldEmail) {
        Cache cache = cacheManager.getCache("usersWithCards");

        if(cache != null) {
            cache.put(updatedUser.getId(), userMapper.toUserWithCards(updatedUser));
        }

        cache = cacheManager.getCache("usersInfo");

        if(cache != null) {
            if(!oldEmail.equals(updatedUser.getEmail())) {
                cache.evict(oldEmail);
            }
            cache.put(updatedUser.getEmail(), userMapper.toUserResponseDto(updatedUser));
        }
    }

    private void deleteUserCache(Long id, String userEmail) {
        Cache cache = cacheManager.getCache("usersWithCards");
        if(cache != null) {
            cache.evict(id);
        }

        cache = cacheManager.getCache("usersInfo");
        if(cache != null) {
            cache.evict(userEmail);
        }
    }
}