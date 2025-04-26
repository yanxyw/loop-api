package com.loop.api.modules.user.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_oauth", indexes = {
		@Index(name = "idx_user_oauth_provider", columnList = "provider,providerId")
})
public class UserOAuth {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AuthProvider provider;

	@Column(nullable = false)
	private String providerId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}
