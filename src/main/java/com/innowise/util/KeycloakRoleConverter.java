package com.innowise.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String clientId;

    public KeycloakRoleConverter(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        Stream<String> realmRoles = extractRealmRoles(jwt);
        Stream<String> clientRoles = extractClientRoles(jwt);

        return Stream.concat(realmRoles, clientRoles)
                .map(roleName -> "ROLE_" + roleName.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    private Stream<String> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
            return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast);
        }
        return Stream.empty();
    }

    private Stream<String> extractClientRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");

        if (resourceAccess != null && resourceAccess.get(clientId) instanceof Map<?, ?> clientAccess) {
            if (clientAccess.get("roles") instanceof Collection<?> roles) {
                return roles.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast);
            }
        }
        return Stream.empty();
    }
}