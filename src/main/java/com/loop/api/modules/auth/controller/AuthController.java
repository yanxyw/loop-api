package com.loop.api.modules.auth.controller;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.common.dto.response.StandardResponse;
import com.loop.api.common.exception.InvalidTokenException;
import com.loop.api.modules.auth.dto.LoginRequest;
import com.loop.api.modules.auth.dto.LoginResponse;
import com.loop.api.modules.auth.dto.RegisterRequest;
import com.loop.api.modules.auth.model.RefreshToken;
import com.loop.api.modules.auth.service.AuthService;
import com.loop.api.modules.auth.service.RefreshTokenService;
import com.loop.api.modules.user.model.User;
import com.loop.api.security.JwtTokenProvider;
import com.loop.api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication", description = "Endpoints for user auth related operations")
public class AuthController {

	private final AuthService authService;
	private final RefreshTokenService refreshTokenService;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthController(AuthService authService, RefreshTokenService refreshTokenService,
						  JwtTokenProvider jwtTokenProvider) {
		this.authService = authService;
		this.refreshTokenService = refreshTokenService;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Operation(summary = "Register a new user", description = "Creates a user with email, password, and username.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "User created"),
			@ApiResponse(responseCode = "400", description = "Validation failed"),
			@ApiResponse(responseCode = "409", description = "User already exists"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@PostMapping(ApiRoutes.Auth.SIGNUP)
	public ResponseEntity<StandardResponse<String>> signup(@Valid @RequestBody RegisterRequest request) {
		String response = authService.registerUser(request);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(StandardResponse.success(HttpStatus.CREATED, "User registered", response));
	}

	@Operation(
			summary = "Login user",
			description = "Authenticates a user and returns an access token in the response body. " +
					"A refresh token is set as an HttpOnly cookie."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Login successful"),
			@ApiResponse(responseCode = "400", description = "Validation failed"),
			@ApiResponse(responseCode = "401", description = "Invalid credentials"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@PostMapping(value = ApiRoutes.Auth.LOGIN, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<StandardResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.loginUser(request);

		RefreshToken refreshToken = refreshTokenService.createRefreshToken(response.getUserId());

		ResponseCookie cookie = refreshTokenService.createRefreshTokenCookie(refreshToken.getToken());

		return ResponseEntity
				.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(StandardResponse.success(HttpStatus.OK, "Login successful", response));
	}

	@Operation(
			summary = "Refresh access token",
			description = "Validates the refresh token from the cookie, issues a new access token, and sets a new " +
					"refresh token in a HttpOnly cookie."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized - refresh token is missing or invalid"),
			@ApiResponse(responseCode = "500", description = "Unexpected server error")
	})
	@PostMapping(ApiRoutes.Auth.REFRESH)
	public ResponseEntity<StandardResponse<LoginResponse>> refreshToken(
			@CookieValue(value = "refreshToken", required = false)
			String refreshTokenStr) {
		if (refreshTokenStr == null) {
			throw new InvalidTokenException("Refresh token is missing.");
		}

		RefreshToken oldToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
		User user = oldToken.getUser();

		// Delete old and create new token
		refreshTokenService.deleteByToken(oldToken.getToken());
		RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
		UserPrincipal userPrincipal = new UserPrincipal(user);
		String newAccessToken = jwtTokenProvider.generateToken(userPrincipal);

		ResponseCookie cookie = refreshTokenService.createRefreshTokenCookie(newRefreshToken.getToken());

		return ResponseEntity
				.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(StandardResponse.success(HttpStatus.OK, "Token refreshed",
						LoginResponse.builder().accessToken(newAccessToken).userId(user.getId()).build()));
	}
}