package com.loop.api.modules.auth.service;

import com.loop.api.common.exception.InvalidCredentialsException;
import com.loop.api.common.exception.UserNotFoundException;
import com.loop.api.common.util.UserValidationUtil;
import com.loop.api.modules.auth.dto.LoginRequest;
import com.loop.api.modules.auth.dto.LoginResponse;
import com.loop.api.modules.auth.dto.RegisterRequest;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import com.loop.api.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
					   JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	public String registerUser(RegisterRequest request) {
		UserValidationUtil.validateUniqueUserFields(
				request.getEmail(), request.getUsername(), null, userRepository, null);

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
		try {
			Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

			if (userOptional.isEmpty()) {
				throw new UserNotFoundException("No account found with this email");
			}

			if (!passwordEncoder.matches(request.getPassword(),
					userOptional.get().getPassword())) {
				throw new InvalidCredentialsException("Invalid email or password");
			}

			String token = jwtTokenProvider.generateToken(request.getEmail());
			return LoginResponse.builder()
					.token(token)
					.build();
		} catch (UserNotFoundException | InvalidCredentialsException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error during login: " + e.getMessage());
		}
	}
}
