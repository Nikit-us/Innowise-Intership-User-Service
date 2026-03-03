package com.innowise.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class RabbitConfig {

    private final ObjectMapper objectMapper;

    public static final String KEYCLOAK_EVENTS_EXCHANGE = "keycloak.events";
    public static final String USER_SERVICE_QUEUE = "user_service_keycloak_queue";

    @Bean
    public TopicExchange keycloakExchange() {
        return new TopicExchange(KEYCLOAK_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue userServiceQueue() {
        return new Queue(USER_SERVICE_QUEUE, true);
    }

    @Bean
    public Binding binding(Queue userServiceQueue, TopicExchange keycloakExchange) {
        return BindingBuilder.bind(userServiceQueue)
                .to(keycloakExchange)
                .with("keycloak.user.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}