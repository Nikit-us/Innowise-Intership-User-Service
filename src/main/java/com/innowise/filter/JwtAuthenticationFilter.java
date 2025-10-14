package com.innowise.filter;

import com.innowise.dto.ValidateTokenRequest;
import com.innowise.dto.ValidateTokenResponse;
import com.innowise.service.AuthenticationServiceClient;
import com.innowise.util.JwtTokenUtils;
import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationServiceClient authenticationServiceClient;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        Optional<String> token = jwtTokenUtils.extractToken(request);

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            ValidateTokenResponse validation = authenticationServiceClient.validate(new ValidateTokenRequest(token.get()));
            if(validation.valid() && validation.tokenType().equals("access_token")) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        validation.userId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (FeignException fe) {
            log.info("Authentication Failed: {}", fe.status());
            response.setStatus(fe.status());
            return;
        }
        filterChain.doFilter(request, response);
    }
}
