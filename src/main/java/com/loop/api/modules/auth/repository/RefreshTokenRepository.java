package com.loop.api.modules.auth.repository;

import com.loop.api.modules.auth.model.RefreshToken;
import com.loop.api.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByToken(String token);

	void deleteByUser(User user);
}
