package com.loop.api.modules.auth.controller;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.common.dto.response.ApiResponse;
import com.loop.api.modules.auth.dto.LoginRequest;
import com.loop.api.modules.auth.dto.LoginResponse;
import com.loop.api.modules.auth.dto.RegisterRequest;
import com.loop.api.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping(ApiRoutes.Auth.SIGNUP)
	public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody RegisterRequest request) {
		String response = authService.registerUser(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(HttpStatus.CREATED, "User registered", response));
	}

	@PostMapping(value = ApiRoutes.Auth.LOGIN, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.loginUser(request);
		return ResponseEntity
				.ok(ApiResponse.success(HttpStatus.OK, "Login successful", response));
	}
}