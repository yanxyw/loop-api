package com.loop.api.modules.auth.dto;

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
	@Pattern(
			regexp = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$",
			message = "Email format is invalid"
	)
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters")
	private String password;

	@NotBlank(message = "Username is required")
	@Size(min = 1, max = 20, message = "Username must be between 1 and 20 characters")
	@Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "Username can only contain letters, numbers and underscores")
	private String username;
}
