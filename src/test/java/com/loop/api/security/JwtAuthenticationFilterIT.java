package com.loop.api.security;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.modules.user.controller.UserController;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import com.loop.api.testutils.TestUserFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("IntegrationTest")
@SpringBootTest
@AutoConfigureMockMvc
@Import(UserController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JwtAuthenticationFilterIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	private User testUser;
	private String jwt;

	@BeforeAll
	void setUp() {
		// Create and save user using the factory
		testUser = TestUserFactory.regularUser(null);
		testUser = userRepository.save(testUser);

		// Generate valid JWT for test user
		jwt = jwtTokenProvider.generateToken(new UserPrincipal(testUser));
	}

	@Test
	@DisplayName("Should authenticate request with valid JWT token")
	void shouldAuthenticateWithValidToken() throws Exception {
		mockMvc.perform(get(ApiRoutes.User.ME)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(testUser.getEmail()));
	}

	@Test
	@DisplayName("Should return 401 if JWT token is missing")
	void shouldReturnUnauthorizedIfTokenMissing() throws Exception {
		mockMvc.perform(get(ApiRoutes.User.ME))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Should return 401 if JWT token is invalid")
	void shouldReturnUnauthorizedIfTokenInvalid() throws Exception {
		mockMvc.perform(get(ApiRoutes.User.ME)
						.header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Should return null if Authorization header does not start with Bearer")
	void shouldReturnNullIfAuthorizationHeaderIsInvalidFormat() throws Exception {
		mockMvc.perform(get(ApiRoutes.User.ME)
						.header(HttpHeaders.AUTHORIZATION, "Token abc.def.ghi"))
				.andExpect(status().isUnauthorized());
	}

	@DisplayName("Should return 401 if user in JWT token does not exist")
	@Test
	void shouldReturnUnauthorizedIfUserNotFound() throws Exception {
		UserPrincipal ghostUserPrincipal = new UserPrincipal(
				TestUserFactory.regularUser(9999L)
		);

		String validTokenWithGhostUser = jwtTokenProvider.generateToken(ghostUserPrincipal);

		mockMvc.perform(get(ApiRoutes.User.ME)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + validTokenWithGhostUser))
				.andExpect(status().isUnauthorized());
	}
}