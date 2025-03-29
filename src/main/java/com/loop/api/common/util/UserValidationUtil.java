package com.loop.api.common.util;

import com.loop.api.common.exception.UserAlreadyExistsException;
import com.loop.api.modules.user.repository.UserRepository;

public class UserValidationUtil {

	public static void validateUniqueUserFields(String email, String username, String mobile,
												UserRepository userRepository, Long excludeUserId) {
		if (email != null) {
			userRepository.findByEmail(email)
					.filter(user -> !user.getId().equals(excludeUserId))
					.ifPresent(user -> {
						throw new UserAlreadyExistsException("User with email '" + email + "' already exists.");
					});
		}
		if (username != null) {
			userRepository.findByUsername(username)
					.filter(user -> !user.getId().equals(excludeUserId))
					.ifPresent(user -> {
						throw new UserAlreadyExistsException("User with username '" + username + "' already exists.");
					});
		}
		if (mobile != null) {
			userRepository.findByMobile(mobile)
					.filter(user -> !user.getId().equals(excludeUserId))
					.ifPresent(user -> {
						throw new UserAlreadyExistsException("User with mobile '" + mobile + "' already exists.");
					});
		}
	}
}
