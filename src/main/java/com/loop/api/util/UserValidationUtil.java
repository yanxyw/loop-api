package com.loop.api.util;

import com.loop.api.exception.UserAlreadyExistsException;
import com.loop.api.repository.UserRepository;

public class UserValidationUtil {
    public static void validateRequiredFields(String email, String username, String password) {
        if (email == null || email.isBlank() ||
                username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email, username, and password cannot be empty or null");
        }
    }

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
        if (mobile != null && !mobile.isBlank()) {
            userRepository.findByMobile(mobile)
                    .filter(user -> !user.getId().equals(excludeUserId))
                    .ifPresent(user -> {
                        throw new UserAlreadyExistsException("User with mobile '" + mobile + "' already exists.");
                    });
        }
    }

    public static void validateNewUser(String email, String username, String password, String mobile,
                                       UserRepository userRepository) {
        validateRequiredFields(email, username, password);
        // For a new user, excludeUserId is null
        validateUniqueUserFields(email, username, mobile, userRepository, null);
    }
}
