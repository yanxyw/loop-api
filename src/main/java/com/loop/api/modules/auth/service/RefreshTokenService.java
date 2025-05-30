package com.loop.api.modules.auth.service;

import com.loop.api.common.exception.InvalidTokenException;
import com.loop.api.common.exception.UserNotFoundException;
import com.loop.api.modules.auth.model.RefreshToken;
import com.loop.api.modules.auth.repository.RefreshTokenRepository;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final Long refreshTokenDurationMs;

	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
							   UserRepository userRepository,
							   @Value("${jwt.refreshExpirationMs}") Long refreshTokenDurationMs) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.userRepository = userRepository;
		this.refreshTokenDurationMs = refreshTokenDurationMs;
	}

	public RefreshToken createRefreshToken(Long userId) {
		RefreshToken token = new RefreshToken();
		token.setUser(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found " +
				"with ID: " + userId)));
		token.setToken(UUID.randomUUID().toString());
		token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

		return refreshTokenRepository.save(token);
	}

	public boolean isExpired(RefreshToken token) {
		return token.getExpiryDate().isBefore(Instant.now());
	}

	public void deleteByUserId(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

		refreshTokenRepository.deleteByUser(user);
	}

	public void deleteByToken(String token) {
		refreshTokenRepository.findByToken(token)
				.ifPresent(refreshTokenRepository::delete);
	}

	public RefreshToken verifyRefreshToken(String tokenStr) {
		return refreshTokenRepository.findByToken(tokenStr)
				.filter(token -> !isExpired(token))
				.orElseThrow(() -> new InvalidTokenException("Refresh token is invalid or expired"));
	}
}

