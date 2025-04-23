package com.loop.api.scheduler;

import com.loop.api.modules.auth.repository.PasswordResetCodeRepository;
import com.loop.api.modules.auth.repository.RefreshTokenRepository;
import com.loop.api.modules.auth.repository.VerificationTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TokenCleanupJob {

	private final RefreshTokenRepository refreshTokenRepository;
	private final VerificationTokenRepository verificationTokenRepository;
	private final PasswordResetCodeRepository passwordResetCodeRepository;

	public TokenCleanupJob(RefreshTokenRepository refreshTokenRepository,
						   VerificationTokenRepository verificationTokenRepository,
						   PasswordResetCodeRepository passwordResetCodeRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.verificationTokenRepository = verificationTokenRepository;
		this.passwordResetCodeRepository = passwordResetCodeRepository;
	}

	@Scheduled(cron = "0 0 2 * * ?", zone = "UTC") // Every day at 2 AM
	public void cleanExpiredRefreshTokens() {
		refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
	}

	@Scheduled(cron = "0 0 2 * * ?", zone = "UTC")
	public void cleanExpiredVerificationTokens() {
		verificationTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
	}

	@Scheduled(cron = "0 0 2 * * ?", zone = "UTC")
	public void cleanExpiredPasswordResetCodes() {
		passwordResetCodeRepository.deleteAllByExpiryDateBefore(Instant.now());
	}
}