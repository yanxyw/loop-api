package com.loop.api.testutils;

import com.loop.api.modules.user.model.User;

public class TestUserFactory {
	public static User regularUser(Long id) {
		User user = new User();
		user.setId(id);
		user.setEmail("user" + id + "@example.com");
		user.setUsername("user" + id);
		user.setMobile("123456789" + id);
		user.setAdmin(false);
		user.setProfileUrl("https://example.com/user" + id + ".png");
		user.setPassword("password123");
		return user;
	}

	public static User adminUser(Long id) {
		User user = regularUser(id);
		user.setAdmin(true);
		return user;
	}
}
