package com.innowise.rabbit;

import java.util.Map;

public record KeycloakEventMessage(
        String action,
        String realmId,
        String userId,
        Map<String, Object> payload,
        long timestamp
) {}