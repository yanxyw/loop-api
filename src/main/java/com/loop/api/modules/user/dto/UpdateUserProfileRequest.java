package com.loop.api.modules.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileRequest {
	@Email(message = "Email format is invalid")
	@Pattern(
			regexp = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$",
			message = "Email format is invalid"
	)
	private String email;

	@Pattern(
			regexp = "^\\+?[0-9]{10,15}$",
			message = "Mobile number is not valid"
	)
	private String mobile;

	@Size(min = 1, max = 20, message = "Username must be between 1 and 20 characters")
	@Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "Username can only contain letters, numbers and underscores")
	private String username;

	@Pattern(
			regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
			message = "Profile URL must be a valid URL"
	)
	private String profileUrl;
}
