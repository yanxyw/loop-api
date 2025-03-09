package com.babelbeats.api.service;

import com.babelbeats.api.dto.AuthRequest;
import com.babelbeats.api.dto.LoginRequest;
import com.babelbeats.api.dto.LoginResponse;
import com.babelbeats.api.exception.InvalidCredentialsException;
import com.babelbeats.api.exception.UserAlreadyExistsException;
import com.babelbeats.api.model.User;
import com.babelbeats.api.repository.UserRepository;
import com.babelbeats.api.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String registerUser(AuthRequest request) {
        if (request.getEmail() == null || request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email, username, and password cannot be null");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        try {
            User user = new User();
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            userRepository.save(user);
            return "User registered successfully";
        } catch (Exception e) {
            throw new RuntimeException("Error registering user: " + e.getMessage());
        }
    }

    public LoginResponse loginUser(LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password cannot be null");
        }

        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

            if (userOptional.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }

            String token = jwtTokenProvider.generateToken(request.getEmail());
            return LoginResponse.builder()
                    .token(token)
                    .email(request.getEmail())
                    .message("Login successful")
                    .build();
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error during login: " + e.getMessage());
        }
    }
}
