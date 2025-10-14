package com.innowise.service;

import com.innowise.dto.ValidateTokenRequest;
import com.innowise.dto.ValidateTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "authentication-service", url = "${auth.service.url}")
public interface AuthenticationServiceClient {
    @PostMapping("/validate")
    ValidateTokenResponse validate(@RequestBody ValidateTokenRequest request);
}