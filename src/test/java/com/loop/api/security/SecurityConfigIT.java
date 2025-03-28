package com.loop.api.security;

import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import com.loop.api.testutils.PostgresTestContainerConfig;
import com.loop.api.testutils.TestUserFactory;
import org.hamcrest.Matchers;
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
@Import(PostgresTestContainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SecurityConfigIT {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@BeforeEach
	void cleanDb() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("Should return 401 for unauthenticated access to protected endpoint")
	void shouldReturn401WhenUnauthenticated() throws Exception {
		mockMvc.perform(get(ApiRoutes.User.ME))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.code").value(401))
				.andExpect(jsonPath("$.message").value(Matchers.containsString("Unauthorized")));
	}

	@Test
	@DisplayName("Should return 403 Forbidden when non-admin user accesses admin endpoint")
	void shouldReturn403WhenNonAdminAccessesAdminEndpoint() throws Exception {
		User user = userRepository.save(TestUserFactory.regularUser(null));
		String jwt = jwtTokenProvider.generateToken(new UserPrincipal(user));

		mockMvc.perform(get(ApiRoutes.Admin.USERS)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value("ERROR"))
				.andExpect(jsonPath("$.code").value(403))
				.andExpect(jsonPath("$.message").value("Forbidden: You do not have permission."));
	}
}