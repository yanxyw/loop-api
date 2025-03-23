package com.loop.api.modules.auth.controller;

import com.loop.api.common.dto.response.ApiResponse;
import com.loop.api.modules.auth.dto.LoginRequest;
import com.loop.api.modules.auth.dto.LoginResponse;
import com.loop.api.modules.auth.dto.RegisterRequest;
import com.loop.api.modules.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(@RequestBody RegisterRequest request) {
        String response = authService.registerUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "User registered", response));
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.loginUser(request);
        return ResponseEntity
                .ok(ApiResponse.success(HttpStatus.OK, "Login successful", response));
    }
}