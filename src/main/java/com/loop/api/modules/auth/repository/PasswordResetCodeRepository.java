package com.loop.api.modules.auth.repository;

import com.loop.api.modules.auth.model.PasswordResetCode;
import com.loop.api.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
	Optional<PasswordResetCode> findByUserEmailAndCode(String email, String code);

	void deleteByUser(User user);

	@Transactional
	void deleteAllByExpiryDateBefore(Instant expiryDate);
}
