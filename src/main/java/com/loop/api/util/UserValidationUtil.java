package com.loop.api.util;

import com.loop.api.exception.UserAlreadyExistsException;
import com.loop.api.repository.UserRepository;

public class UserValidationUtil {
    public static void validateNewUser(String email, String username, String password, UserRepository userRepository) {
        if (email == null || email.isBlank() ||
                username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email, username, and password cannot be empty or null");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User with email '" + email + "' already exists.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("User with username '" + username + "' already exists.");
        }
    }
}
