package com.loop.api.security;

import com.loop.api.modules.user.model.User;
import com.loop.api.testutils.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("UnitTest")
public class JwtTokenProviderTest {

	private final String secret = "my-secret-key-123456789012345678901234"; // must be at least 32 bytes
	private final long expirationTime = 3600000L; // 1 hour
	private JwtTokenProvider jwtTokenProvider;

	@BeforeEach
	void setUp() {
		jwtTokenProvider = new JwtTokenProvider(secret, expirationTime);
	}

	@Test
	void shouldGenerateAndValidateToken() {
		User user = TestUserFactory.regularUser(1L);
		UserPrincipal principal = new UserPrincipal(user);

		String token = jwtTokenProvider.generateToken(principal);
		assertNotNull(token);
		assertTrue(jwtTokenProvider.validateToken(token));
	}

	@Test
	void shouldExtractUserIdFromToken() {
		User user = TestUserFactory.regularUser(1L);
		UserPrincipal principal = new UserPrincipal(user);
		String token = jwtTokenProvider.generateToken(principal);

		String userId = jwtTokenProvider.getUserIdFromToken(token);
		assertEquals("1", userId);
	}

	@Test
	void shouldReturnFalseForInvalidToken() {
		assertFalse(jwtTokenProvider.validateToken("invalid.token.string"));
	}
}