package com.innowise.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.dto.user.UserCreateDto;
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
        log.info("Action: {}", message.action());
        if ("USER_CREATE".equals(message.action())) {
            UserCreateDto createDto = objectMapper.convertValue(message.payload(), UserCreateDto.class);

            log.info("User create: {}", createDto.email());
            userService.createUser(createDto);
        }
    }
}