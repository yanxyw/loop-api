package com.loop.api.testutils;

import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.model.User;

public class TestUserResponseFactory {
	public static UserResponse fromUser(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setEmail(user.getEmail());
		response.setMobile(user.getMobile());
		response.setUsername(user.getUsername());
		response.setAdmin(user.isAdmin());
		response.setProfileUrl(user.getProfileUrl());
		return response;
	}
}
