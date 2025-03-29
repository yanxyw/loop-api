package com.loop.api.security;

import com.loop.api.modules.user.model.User;
import com.loop.api.testutils.TestUserFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
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
	void shouldGenerateAndParseToken() {
		User user = TestUserFactory.regularUser(1L);
		UserPrincipal principal = new UserPrincipal(user);

		String token = jwtTokenProvider.generateToken(principal);
		assertNotNull(token);

		Jws<Claims> claims = jwtTokenProvider.parseToken(token);
		assertNotNull(claims);
		assertEquals("1", claims.getPayload().getSubject());
	}

	@Test
	void shouldExtractUserIdFromParsedToken() {
		User user = TestUserFactory.regularUser(1L);
		UserPrincipal principal = new UserPrincipal(user);

		String token = jwtTokenProvider.generateToken(principal);
		Jws<Claims> claims = jwtTokenProvider.parseToken(token);

		String userId = jwtTokenProvider.getUserIdFromClaims(claims);
		assertEquals("1", userId);
	}

	@Test
	void shouldThrowExceptionForInvalidToken() {
		assertThrows(JwtException.class, () -> {
			jwtTokenProvider.parseToken("invalid.token.string");
		});
	}
}