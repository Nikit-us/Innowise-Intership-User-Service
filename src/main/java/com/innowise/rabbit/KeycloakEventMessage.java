package com.innowise.rabbit;

import java.util.Map;
import java.util.UUID;

public record KeycloakEventMessage(
        String action,
        UUID realmId,
        UUID userId,
        Map<String, Object> payload,
        long timestamp
) {}