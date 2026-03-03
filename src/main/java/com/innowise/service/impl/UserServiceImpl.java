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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public void createUser(UUID id, UserCreateDto userCreateDto) {
        User entity = userMapper.toUser(userCreateDto);
        entity.setId(id);
        userRepository.save(entity);
    }

    @Override
    @Cacheable(value = "usersWithCards", key = "#id")
    @Transactional(readOnly = true)
    public UserWithCardsDto getUserById(UUID id) {
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
    public List<UserResponseDto> getUsersByIds(List<UUID> ids) {
        List<User> users = userRepository.findAllById(ids);
        return userMapper.toUserResponseDto(users);
    }

    @Override
    @Transactional
    public void updateUser(UUID id, UserUpdateDto userUpdateDto) {
        User user = findUserById(id);
        String oldEmail = user.getEmail();
        userMapper.merge(userUpdateDto, user);

        User updatedUser = userRepository.save(user);

        updateUsersCache(updatedUser, oldEmail);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        deleteUserCache(id, findUserById(id).getEmail());
        userRepository.deleteById(id);
    }

    private User findUserById(UUID id) {
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

    private void deleteUserCache(UUID id, String userEmail) {
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