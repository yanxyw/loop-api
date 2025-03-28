package com.loop.api.modules.auth.service;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.common.exception.InvalidTokenException;
import com.loop.api.common.exception.UserNotFoundException;
import com.loop.api.modules.auth.model.RefreshToken;
import com.loop.api.modules.auth.repository.RefreshTokenRepository;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

	private final Long userId = 1L;
	@Mock
	private RefreshTokenRepository refreshTokenRepository;
	@Mock
	private UserRepository userRepository;
	private RefreshTokenService refreshTokenService;

	@BeforeEach
	void setUp() {
		refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository, 3600000L);
	}

	@Nested
	@DisplayName("Tests for createRefreshToken")
	class CreateRefreshToken {

		@Test
		@DisplayName("Should create a refresh token successfully")
		void shouldCreateTokenSuccessfully() {
			User mockUser = new User();
			mockUser.setId(userId);

			when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
			when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

			RefreshToken token = refreshTokenService.createRefreshToken(userId);

			assertNotNull(token.getToken());
			assertNotNull(token.getExpiryDate());
			assertEquals(mockUser, token.getUser());
		}

		@Test
		@DisplayName("Should throw UserNotFoundException if user does not exist")
		void shouldThrowIfUserNotFound() {
			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			assertThrows(UserNotFoundException.class, () -> refreshTokenService.createRefreshToken(userId));
		}
	}

	@Nested
	@DisplayName("Tests for isExpired")
	class IsExpired {

		@Test
		@DisplayName("Should return true if token is expired")
		void shouldReturnTrueIfExpired() {
			RefreshToken expiredToken = new RefreshToken();
			expiredToken.setExpiryDate(Instant.now().minusSeconds(60));

			assertTrue(refreshTokenService.isExpired(expiredToken));
		}

		@Test
		@DisplayName("Should return false if token is not expired")
		void shouldReturnFalseIfNotExpired() {
			RefreshToken validToken = new RefreshToken();
			validToken.setExpiryDate(Instant.now().plusSeconds(60));

			assertFalse(refreshTokenService.isExpired(validToken));
		}
	}

	@Nested
	@DisplayName("Tests for deleteByUserId")
	class DeleteByUserId {

		@Test
		@DisplayName("Should delete refresh tokens by user ID")
		void shouldDeleteTokensByUserId() {
			User mockUser = new User();
			mockUser.setId(userId);

			when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

			refreshTokenService.deleteByUserId(userId);

			verify(userRepository).findById(userId);
			verify(refreshTokenRepository).deleteByUser(mockUser);
		}

		@Test
		@DisplayName("Should throw UserNotFoundException if user not found")
		void shouldThrowUserNotFoundExceptionIfUserNotFound() {
			when(userRepository.findById(userId)).thenReturn(Optional.empty());

			assertThrows(UserNotFoundException.class, () -> refreshTokenService.deleteByUserId(userId));

			verify(refreshTokenRepository, never()).deleteByUser(any());
		}
	}

	@Nested
	@DisplayName("Tests for deleteByToken")
	class DeleteByToken {

		@Test
		@DisplayName("Should delete refresh token if token exists")
		void shouldDeleteIfTokenExists() {
			String tokenStr = "mock-token";
			RefreshToken token = new RefreshToken();
			token.setToken(tokenStr);

			when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

			refreshTokenService.deleteByToken(tokenStr);

			verify(refreshTokenRepository).delete(token);
		}

		@Test
		@DisplayName("Should do nothing if token does not exist")
		void shouldDoNothingIfTokenDoesNotExist() {
			String tokenStr = "missing-token";

			when(refreshTokenRepository.findByToken(tokenStr)).thenReturn(Optional.empty());

			refreshTokenService.deleteByToken(tokenStr);

			verify(refreshTokenRepository, never()).delete(any());
		}
	}

	@Nested
	@DisplayName("Tests for verifyRefreshToken")
	class VerifyRefreshToken {

		private final String validTokenStr = "valid-token";
		private final String expiredTokenStr = "expired-token";
		private final String nonexistentTokenStr = "nonexistent-token";

		@Test
		@DisplayName("Should return token if it is valid and not expired")
		void shouldReturnValidToken() {
			RefreshToken validToken = new RefreshToken();
			validToken.setToken(validTokenStr);
			validToken.setExpiryDate(Instant.now().plusSeconds(3600));

			when(refreshTokenRepository.findByToken(validTokenStr)).thenReturn(Optional.of(validToken));

			RefreshToken result = refreshTokenService.verifyRefreshToken(validTokenStr);

			assertNotNull(result);
			assertEquals(validTokenStr, result.getToken());
		}

		@Test
		@DisplayName("Should throw InvalidTokenException if token is expired")
		void shouldThrowIfTokenExpired() {
			RefreshToken expiredToken = new RefreshToken();
			expiredToken.setToken(expiredTokenStr);
			expiredToken.setExpiryDate(Instant.now().minusSeconds(60));

			when(refreshTokenRepository.findByToken(expiredTokenStr)).thenReturn(Optional.of(expiredToken));

			assertThrows(InvalidTokenException.class, () -> refreshTokenService.verifyRefreshToken(expiredTokenStr));
		}

		@Test
		@DisplayName("Should throw InvalidTokenException if token is not found")
		void shouldThrowIfTokenNotFound() {
			when(refreshTokenRepository.findByToken(nonexistentTokenStr)).thenReturn(Optional.empty());

			assertThrows(InvalidTokenException.class,
					() -> refreshTokenService.verifyRefreshToken(nonexistentTokenStr));
		}
	}

	@Nested
	@DisplayName("Tests for createRefreshTokenCookie")
	class CreateRefreshTokenCookie {

		private final String tokenValue = "test-refresh-token";

		@BeforeEach
		void setupDuration() {
			// Ensure duration is set before running the test
			refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository, 3600000L); // 1 hour
		}

		@Test
		@DisplayName("Should create cookie with correct attributes")
		void shouldCreateCookieWithExpectedAttributes() {
			ResponseCookie cookie = refreshTokenService.createRefreshTokenCookie(tokenValue);

			assertEquals("refreshToken", cookie.getName());
			assertEquals(tokenValue, cookie.getValue());
			assertTrue(cookie.isHttpOnly());
			assertTrue(cookie.isSecure());
			assertEquals(ApiRoutes.CONTEXT_PATH + ApiRoutes.Auth.REFRESH, cookie.getPath());
			assertEquals("Strict", cookie.getSameSite());
			assertEquals(3600, cookie.getMaxAge().getSeconds());
		}
	}
}
