package com.loop.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
	@NotBlank(message = "Email is required")
	@Email(message = "Email format is invalid")
	@Schema(description = "User's email address", example = "user@example.com")
	@Pattern(
			regexp = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$",
			message = "Email format is invalid"
	)
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters")
	@Schema(description = "Secure password", example = "securePass123")
	private String password;

	@NotBlank(message = "Username is required")
	@Size(min = 1, max = 20, message = "Username must be between 1 and 20 characters")
	@Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "Username can only contain letters, numbers and underscores")
	@Schema(description = "Unique username", example = "test_user")
	private String username;
}
