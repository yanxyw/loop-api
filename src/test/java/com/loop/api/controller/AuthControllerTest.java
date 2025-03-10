package com.loop.api.controller;

import com.loop.api.config.SecurityConfig;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({AuthControllerTestConfig.class, SecurityConfig.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSignup() throws Exception {
        // Arrange: create a sample AuthRequest
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("testuser@example.com");
        registerRequest.setPassword("test1234");

        // Stub the service call
        String expectedResponse = "User registered successfully";
        Mockito.when(authService.registerUser(Mockito.any(RegisterRequest.class)))
                .thenReturn(expectedResponse);

        // Act & Assert: perform the POST request and verify response
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    public void testLogin() throws Exception {
        // Arrange: create a sample LoginRequest
        LoginRequest loginRequest = LoginRequest.builder()
                .email("testuser@example.com")
                .password("test1234")
                .build();


        // Create a sample LoginResponse using the builder
        LoginResponse loginResponse = LoginResponse.builder()
                .token("abc123")
                .email("testuser@example.com")
                .message("Login successful")
                .build();

        // Stub the loginUser method
        Mockito.when(authService.loginUser(Mockito.any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert: perform the POST request and verify the JSON response
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("abc123"));
    }
}
