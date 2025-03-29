package com.loop.api.modules.auth.model;

import com.loop.api.modules.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "refresh_tokens", indexes = {
		@Index(name = "idx_token", columnList = "token"),
		@Index(name = "idx_user_id", columnList = "user_id"),
		@Index(name = "idx_expiry_date", columnList = "expiryDate")
})
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String token;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private Instant expiryDate;
}