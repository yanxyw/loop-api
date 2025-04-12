package com.loop.api.modules.auth.service;

import com.loop.api.common.exception.InvalidCredentialsException;
import com.loop.api.common.exception.UserNotFoundException;
import com.loop.api.common.util.UserValidationUtil;
import com.loop.api.modules.auth.dto.LoginRequest;
import com.loop.api.modules.auth.dto.LoginResponse;
import com.loop.api.modules.auth.dto.RegisterRequest;
import com.loop.api.modules.auth.model.RefreshToken;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import com.loop.api.security.JwtTokenProvider;
import com.loop.api.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final AuthenticationManager authenticationManager;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
					   JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService,
					   AuthenticationManager authenticationManager
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshTokenService = refreshTokenService;
		this.authenticationManager = authenticationManager;
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
		// First, check if the user exists
		Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
		if (userOpt.isEmpty()) {
			throw new UserNotFoundException("This email is not registered. Would you like to sign up instead?");
		}

		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.getEmail(),
							request.getPassword()
					)
			);

			// Authentication succeeded, get UserPrincipal
			UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

			// Now generate JWT using their email or username
			String accessToken = jwtTokenProvider.generateToken(userPrincipal);

			// Generate refresh token and save to DB
			RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

			return LoginResponse.builder()
					.userId(userPrincipal.getId())
					.accessToken(accessToken)
					.refreshToken(refreshToken.getToken())
					.build();

		} catch (AuthenticationException ex) {
			throw new InvalidCredentialsException("Invalid email or password");
		}
	}
}
