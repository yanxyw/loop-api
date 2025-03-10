package com.loop.api.controller;

import com.loop.api.service.AuthService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AuthControllerTestConfig {

    @Bean
    public AuthService authService() {
        // Create and return a Mockito mock for UserService
        return Mockito.mock(AuthService.class);
    }
}
