package com.innowise.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.dto.user.UserCreateDto;
import com.innowise.dto.user.UserUpdateDto;
import com.innowise.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakEventListener {
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @RabbitListener(queues = "user_service_keycloak_queue")
    public void handleKeycloakEvent(KeycloakEventMessage message) {
        switch (message.action()) {
            case "USER_CREATE" -> {
                UserCreateDto createDto = objectMapper.convertValue(message.payload(), UserCreateDto.class);
                userService.createUser(message.userId(), createDto);
            }
            case "USER_UPDATE" -> {
                UserUpdateDto updateDto = objectMapper.convertValue(message.payload(), UserUpdateDto.class);
                userService.updateUser(message.userId(), updateDto);
            }
            case "USER_DELETE" -> {
                userService.deleteUser(message.userId());
            }
        }
    }
}