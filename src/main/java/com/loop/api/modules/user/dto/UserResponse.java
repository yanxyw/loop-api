package com.loop.api.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
	private Long id;
	private String email;
	private String mobile;
	private String username;
	private boolean admin;
	private String profileUrl;
}
