package com.innowise.filter;

import com.innowise.dto.ValidateTokenRequest;
import com.innowise.dto.ValidateTokenResponse;
import com.innowise.service.AuthenticationServiceClient;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AuthenticationServiceClient authenticationServiceClient;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        log.info("Authorization header: {}", header);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        log.info("Authentication Token: {}", token);
        try {
            ValidateTokenResponse validation = authenticationServiceClient.validate(new ValidateTokenRequest(token));
            log.info("Validate Token: {}", validation);
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
