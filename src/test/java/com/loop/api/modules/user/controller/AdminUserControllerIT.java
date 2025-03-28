package com.loop.api.modules.user.controller;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.modules.auth.repository.RefreshTokenRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("IntegrationTest")
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestContainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminUserControllerIT {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@BeforeEach
	void setUp() {
		refreshTokenRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("Should fetch all users for admin")
	void shouldFetchAllUsersForAdmin() throws Exception {
		User adminUser = TestUserFactory.randomAdminUser();
		userRepository.save(adminUser);
		userRepository.save(TestUserFactory.randomRegularUser());
		userRepository.save(TestUserFactory.randomRegularUser());

		String jwt = jwtTokenProvider.generateToken(new UserPrincipal(adminUser));

		mockMvc.perform(get(ApiRoutes.Admin.USERS)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("Fetched all users"))
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(3));
	}

	@Test
	@DisplayName("Should fetch a user by ID as admin")
	void shouldFetchUserByIdAsAdmin() throws Exception {
		User admin = userRepository.save(TestUserFactory.randomAdminUser());
		User targetUser = userRepository.save(TestUserFactory.randomRegularUser());

		String jwt = jwtTokenProvider.generateToken(new UserPrincipal(admin));

		mockMvc.perform(get(ApiRoutes.Admin.USERS + "/" + targetUser.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("User fetched"))
				.andExpect(jsonPath("$.data.id").value(targetUser.getId()))
				.andExpect(jsonPath("$.data.email").value(targetUser.getEmail()));
	}

	@Test
	@DisplayName("Should delete user successfully as admin")
	void shouldDeleteUserSuccessfullyAsAdmin() throws Exception {
		User adminUser = userRepository.save(TestUserFactory.randomAdminUser());
		User targetUser = userRepository.save(TestUserFactory.randomRegularUser());

		String jwt = jwtTokenProvider.generateToken(new UserPrincipal(adminUser));

		mockMvc.perform(delete(ApiRoutes.Admin.USERS + "/" + targetUser.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("User deleted successfully"))
				.andExpect(jsonPath("$.data").doesNotExist());

		Optional<User> deletedUser = userRepository.findById(targetUser.getId());
		assertFalse(deletedUser.isPresent());
	}
}