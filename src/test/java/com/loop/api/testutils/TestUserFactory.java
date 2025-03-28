package com.loop.api.testutils;

import com.loop.api.modules.user.model.User;

import java.util.UUID;

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

	public static User randomRegularUser() {
		String randomPart = UUID.randomUUID().toString().substring(0, 8);
		User user = new User();
		user.setEmail("user" + randomPart + "@example.com");
		user.setUsername("user" + randomPart);
		user.setMobile("123456789" + randomPart);
		user.setAdmin(false);
		user.setProfileUrl("https://example.com/user" + randomPart + ".png");
		user.setPassword("password123");
		return user;
	}

	public static User randomAdminUser() {
		User user = randomRegularUser();
		user.setAdmin(true);
		return user;
	}
}