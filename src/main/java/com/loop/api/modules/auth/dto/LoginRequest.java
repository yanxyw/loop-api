package com.loop.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "User login credentials")
public class LoginRequest {
	@NotBlank(message = "Email is required")
	@Email(message = "Email format is invalid")
	@Schema(description = "User's email address", example = "user@example.com")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters")
	@Schema(description = "User's password (min 8 characters)", example = "securePass123")
	private String password;
}