package com.loop.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Login response containing the user ID and access token")
public class LoginResponse {
	@Schema(description = "Unique ID of the authenticated user", example = "42")
	private Long userId;

	@Schema(description = "JWT access token to use in Authorization header", example = "eyJhbGciOiJIUzI1...")
	private String accessToken;

	@Schema(description = "Refresh token used to obtain a new access token when expired", example = "b3f5cbf8-8f6a...")
	private String refreshToken;
}