package com.loop.api.modules.auth.controller;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.common.dto.response.ApiResponse;
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
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
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

		RefreshToken refreshToken = refreshTokenService.createRefreshToken(response.getUserId());

		ResponseCookie cookie = refreshTokenService.createRefreshTokenCookie(refreshToken.getToken());

		return ResponseEntity
				.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(ApiResponse.success(HttpStatus.OK, "Login successful", response));
	}

	@PostMapping(ApiRoutes.Auth.REFRESH)
	public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
			@CookieValue(value = "refreshToken", required = false) String refreshTokenStr) {
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
				.body(ApiResponse.success(HttpStatus.OK, "Token refreshed",
						LoginResponse.builder().token(newAccessToken).userId(user.getId()).build()));
	}
}