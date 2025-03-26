package com.loop.api.modules.user.mapper;

import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.model.User;
import com.loop.api.testutils.TestUserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("UnitTest")
public class UserMapperTest {

	private final UserMapper userMapper = new UserMapper();

	@Test
	@DisplayName("Should map User to UserResponse correctly")
	void shouldMapUserToUserResponse() {
		User user = TestUserFactory.regularUser(1L);

		UserResponse response = userMapper.toUserResponse(user);

		assertEquals(user.getId(), response.getId());
		assertEquals(user.getEmail(), response.getEmail());
		assertEquals(user.getMobile(), response.getMobile());
		assertEquals(user.getUsername(), response.getUsername());
		assertEquals(user.isAdmin(), response.isAdmin());
		assertEquals(user.getProfileUrl(), response.getProfileUrl());
	}

	@Test
	@DisplayName("Should return null when user is null")
	void shouldReturnNullIfUserIsNull() {
		assertNull(userMapper.toUserResponse(null));
	}

	@Test
	@DisplayName("Should map list of Users to list of UserResponses")
	void shouldMapUserList() {
		User user1 = TestUserFactory.regularUser(1L);
		User user2 = TestUserFactory.adminUser(2L);

		List<UserResponse> result = userMapper.toUserResponseList(List.of(user1, user2));

		assertEquals(2, result.size());
		assertEquals(user1.getEmail(), result.get(0).getEmail());
		assertEquals(user2.getEmail(), result.get(1).getEmail());
	}
}
