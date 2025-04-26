package com.loop.api.modules.user.repository;

import com.loop.api.modules.user.model.AuthProvider;
import com.loop.api.modules.user.model.UserOAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOAuthRepository extends JpaRepository<UserOAuth, Long> {
	Optional<UserOAuth> findByUserIdAndProvider(Long userId, AuthProvider provider);
}
