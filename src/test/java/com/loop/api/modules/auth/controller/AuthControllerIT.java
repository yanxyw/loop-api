package com.loop.api.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.modules.auth.dto.LoginRequest;
import com.loop.api.modules.auth.dto.RegisterRequest;
import com.loop.api.modules.auth.model.RefreshToken;
import com.loop.api.modules.auth.repository.RefreshTokenRepository;
import com.loop.api.modules.auth.service.RefreshTokenService;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import com.loop.api.testutils.PostgresTestContainerConfig;
import com.loop.api.testutils.TestUserFactory;
import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("IntegrationTest")
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestContainerConfig.class)
@TestPropertySource(properties = {
		"jwt.secret=super-secure-and-long-secret-key-for-testing",
		"jwt.accessExpirationMs=3600000",
		"jwt.refreshExpirationMs=3600000"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthControllerIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		refreshTokenRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("Should register new user successfully")
	void shouldRegisterUserSuccessfully() throws Exception {
		RegisterRequest request = new RegisterRequest();
		request.setEmail("test@example.com");
		request.setPassword("securePass123");
		request.setUsername("test123");

		mockMvc.perform(post(ApiRoutes.Auth.SIGNUP)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.code").value(201))
				.andExpect(jsonPath("$.message").value("User registered"))
				.andExpect(jsonPath("$.data").value("User registered successfully"));

		// Verify user exists in DB
		Optional<User> userOpt = userRepository.findByEmail("test@example.com");
		assertTrue(userOpt.isPresent());
		assertEquals("test123", userOpt.get().getUsername());
	}

	@Test
	@DisplayName("Should login successfully and return access token + refresh token cookie")
	void shouldLoginSuccessfully() throws Exception {
		// Arrange - create user in DB
		User user = new User();
		user.setEmail("login@example.com");
		user.setPassword(new BCryptPasswordEncoder().encode("securePass123"));
		user.setUsername("loginUser");
		user.setAdmin(false);
		userRepository.save(user);

		// Act + Assert
		LoginRequest request = new LoginRequest();
		request.setEmail("login@example.com");
		request.setPassword("securePass123");

		MvcResult result = mockMvc.perform(post(ApiRoutes.Auth.LOGIN)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("Login successful"))
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.data.userId").value(user.getId()))
				.andReturn();

		// Assert refresh token cookie is set
		String setCookieHeader = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
		assertNotNull(setCookieHeader);
		assertTrue(setCookieHeader.contains("refreshToken="));

		// Assert refresh token saved in DB
		List<RefreshToken> tokens = refreshTokenRepository.findAll();
		assertEquals(1, tokens.size());
		assertEquals(user.getId(), tokens.getFirst().getUser().getId());
	}

	@Test
	@DisplayName("Should refresh token successfully with valid refresh token cookie")
	void shouldRefreshTokenSuccessfully() throws Exception {
		// Arrange - create and save user
		User user = TestUserFactory.regularUser(null);
		user = userRepository.save(user);

		// Create and persist refresh token
		RefreshToken oldRefreshToken = refreshTokenService.createRefreshToken(user.getId());
		assertTrue(refreshTokenRepository.findByToken(oldRefreshToken.getToken()).isPresent());

		// Act - perform request with cookie
		MvcResult result = mockMvc.perform(post(ApiRoutes.Auth.REFRESH)
						.cookie(new Cookie("refreshToken", oldRefreshToken.getToken())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("Token refreshed"))
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.data.userId").value(user.getId()))
				.andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("refreshToken")))
				.andReturn();

		// Extract the new refresh token from Set-Cookie header
		String setCookieHeader = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
		assert setCookieHeader != null;
		String newRefreshTokenValue = Arrays.stream(setCookieHeader.split(";"))
				.filter(s -> s.startsWith("refreshToken="))
				.findFirst()
				.map(s -> s.replace("refreshToken=", ""))
				.orElseThrow(() -> new AssertionError("New refresh token not found in cookie"));

		// Assert - old token is deleted
		assertFalse(refreshTokenRepository.findByToken(oldRefreshToken.getToken()).isPresent());

		// Assert - new token is saved
		Optional<RefreshToken> newTokenOpt = refreshTokenRepository.findByToken(newRefreshTokenValue);
		assertTrue(newTokenOpt.isPresent(), "New refresh token should be saved in DB");

		// Assert - associated user is still correct
		assertEquals(user.getId(), newTokenOpt.get().getUser().getId(), "New token should belong to same user");
	}
}