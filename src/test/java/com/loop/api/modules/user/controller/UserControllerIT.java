package com.loop.api.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.modules.auth.repository.RefreshTokenRepository;
import com.loop.api.modules.user.dto.UpdateUserProfileRequest;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import com.loop.api.security.JwtTokenProvider;
import com.loop.api.security.UserPrincipal;
import com.loop.api.testutils.PostgresTestContainerConfig;
import com.loop.api.testutils.TestUserFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("IntegrationTest")
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestContainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerIT {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	@Autowired
	private ObjectMapper objectMapper;

	private User testUser;
	private String jwt;

	@BeforeEach
	void setUp() {
		refreshTokenRepository.deleteAll();
		userRepository.deleteAll();
		testUser = userRepository.save(TestUserFactory.regularUser(null));
		jwt = jwtTokenProvider.generateToken(new UserPrincipal(testUser));
	}

	@Test
	@DisplayName("Should return user profile when authenticated")
	void shouldReturnUserProfile() throws Exception {
		mockMvc.perform(get(ApiRoutes.User.ME)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.data.email").value(testUser.getEmail()))
				.andExpect(jsonPath("$.data.username").value(testUser.getUsername()))
				.andExpect(jsonPath("$.data.id").value(testUser.getId()));
	}

	@Test
	@DisplayName("Should update user profile successfully")
	void shouldUpdateUserProfileSuccessfully() throws Exception {
		UpdateUserProfileRequest request = new UpdateUserProfileRequest();
		request.setEmail("updated@example.com");
		request.setUsername("updatedUser");
		request.setMobile("9876543210");
		request.setProfileUrl("https://example.com/new-pic.png");

		mockMvc.perform(put(ApiRoutes.User.ME)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("User profile updated"))
				.andExpect(jsonPath("$.data.email").value("updated@example.com"))
				.andExpect(jsonPath("$.data.username").value("updatedUser"))
				.andExpect(jsonPath("$.data.mobile").value("9876543210"))
				.andExpect(jsonPath("$.data.profileUrl").value("https://example.com/new-pic.png"));

		// Verify DB directly
		User updated = userRepository.findById(testUser.getId()).orElseThrow();
		assertEquals("updated@example.com", updated.getEmail());
		assertEquals("updatedUser", updated.getUsername());
		assertEquals("9876543210", updated.getMobile());
	}

	@Test
	@DisplayName("Should delete user account successfully with valid JWT")
	void shouldDeleteMyAccountSuccessfully() throws Exception {
		// Verify user exists
		assertTrue(userRepository.existsById(testUser.getId()));

		// Perform DELETE request with JWT
		mockMvc.perform(delete(ApiRoutes.User.ME)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("User deleted successfully"))
				.andExpect(jsonPath("$.data").doesNotExist());

		// Assert user no longer exists
		assertFalse(userRepository.existsById(testUser.getId()));
	}
}