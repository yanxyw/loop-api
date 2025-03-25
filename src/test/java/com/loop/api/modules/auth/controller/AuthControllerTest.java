package com.loop.api.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loop.api.modules.auth.dto.LoginRequest;
import com.loop.api.modules.auth.dto.LoginResponse;
import com.loop.api.modules.auth.dto.RegisterRequest;
import com.loop.api.modules.auth.service.AuthService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("UnitTest")
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void testSignup() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPassword("test1234");

        Mockito.when(authService.registerUser(any(RegisterRequest.class)))
                .thenReturn("User registered successfully");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("User registered"))
                .andExpect(jsonPath("$.data").value("User registered successfully"));
    }

    @Test
    public void testLogin() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("testuser@example.com")
                .password("test1234")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .token("abc123")
                .build();

        Mockito.when(authService.loginUser(any(LoginRequest.class)))
                .thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("abc123"));
    }
}