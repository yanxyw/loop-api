package com.loop.api.controller;

import com.loop.api.dto.RegisterRequest;
import com.loop.api.dto.LoginRequest;
import com.loop.api.dto.LoginResponse;
import com.loop.api.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Test
    void testSignup() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPassword("test1234");

        // Stub the mock in your test (the mock is from AuthControllerTestConfig)
        Mockito.when(authService.registerUser(any(RegisterRequest.class)))
                .thenReturn("User registered successfully");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    public void testLogin() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("testuser@example.com")
                .password("test1234")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .token("abc123")
                .message("Login successful")
                .build();

        Mockito.when(authService.loginUser(any(LoginRequest.class)))
                .thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("abc123"));
    }
}
