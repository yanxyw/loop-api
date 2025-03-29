package com.loop.api.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Public representation of a user")
public class UserResponse {
	@Schema(description = "Unique ID of the user", example = "1")
	private Long id;

	@Schema(description = "Email address", example = "user@example.com")
	private String email;

	@Schema(description = "Mobile phone number", example = "+1234567890")
	private String mobile;

	@Schema(description = "Unique username", example = "test_user")
	private String username;

	@Schema(description = "Whether the user is an admin", example = "false")
	private boolean admin;

	@Schema(description = "Profile picture URL", example = "https://cdn.example.com/profiles/user1.jpg")
	private String profileUrl;
}
