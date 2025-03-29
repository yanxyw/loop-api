package com.loop.api.scheduler;

import com.loop.api.modules.auth.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RefreshTokenCleanupJob {

	private final RefreshTokenRepository refreshTokenRepository;

	public RefreshTokenCleanupJob(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Scheduled(cron = "0 0 2 * * ?", zone = "UTC") // Every day at 2 AM
	public void cleanExpiredTokens() {
		refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
	}
}