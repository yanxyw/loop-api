package com.loop.api.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loop.api.common.exception.UserNotFoundException;
import com.loop.api.modules.user.dto.UpdateUserProfileRequest;
import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.mapper.UserMapper;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.service.UserService;
import com.loop.api.security.UserPrincipal;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("UnitTest")
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private UserMapper userMapper;

	@Autowired
	private ObjectMapper objectMapper;

	private User user;
	private UserResponse userResponse;
	private UserPrincipal userPrincipal;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setUsername("testuser");
		user.setMobile("1234567890");
		user.setProfileUrl("http://example.com/avatar.png");
		user.setAdmin(false);

		userResponse = new UserResponse();
		userResponse.setId(user.getId());
		userResponse.setEmail(user.getEmail());
		userResponse.setUsername(user.getUsername());
		userResponse.setMobile(user.getMobile());
		userResponse.setProfileUrl(user.getProfileUrl());
		userResponse.setAdmin(user.isAdmin());

		userPrincipal = new UserPrincipal(user);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private RequestPostProcessor authenticated(UserPrincipal principal) {
		Authentication auth = new UsernamePasswordAuthenticationToken(
				principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
		return SecurityMockMvcRequestPostProcessors.authentication(auth);
	}

	@Nested
	@DisplayName("Tests for GET /me")
	class GetMyProfileTests {
		@Test
		@DisplayName("Should return 200 and user profile when authenticated")
		void shouldReturnUserProfileWhenAuthenticated() throws Exception {
			when(userService.getUserById(user.getId())).thenReturn(userResponse);

			mockMvc.perform(get("/users/me")
							.with(authenticated(userPrincipal)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("SUCCESS"))
					.andExpect(jsonPath("$.code").value(200))
					.andExpect(jsonPath("$.message").value("User profile fetched"))
					.andExpect(jsonPath("$.data.id").value(user.getId()))
					.andExpect(jsonPath("$.data.email").value(user.getEmail()))
					.andExpect(jsonPath("$.data.mobile").value(user.getMobile()))
					.andExpect(jsonPath("$.data.username").value(user.getUsername()))
					.andExpect(jsonPath("$.data.admin").value(user.isAdmin()))
					.andExpect(jsonPath("$.data.profileUrl").value(user.getProfileUrl()));
		}

		@Test
		@DisplayName("Should return 404 if user not found")
		void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
			when(userService.getUserById(user.getId()))
					.thenThrow(new UserNotFoundException("User not found with id: " + user.getId()));

			mockMvc.perform(get("/users/me")
							.with(authenticated(userPrincipal)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(404))
					.andExpect(jsonPath("$.message").value("User not found with id: " + user.getId()));
		}
	}

	@Nested
	@DisplayName("Tests for PUT /me")
	class UpdateMyProfileTests {

		@Test
		@DisplayName("Should return 200 and updated profile when valid request is made")
		void shouldUpdateProfileSuccessfully() throws Exception {
			UpdateUserProfileRequest req = new UpdateUserProfileRequest();
			req.setEmail("updated@example.com");

			userResponse.setEmail(req.getEmail());

			when(userService.updateUserProfile(eq(user.getId()), any())).thenReturn(userResponse);

			mockMvc.perform(put("/users/me")
							.with(authenticated(userPrincipal))
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(req)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.email").value(req.getEmail()));
		}

		@Test
		@DisplayName("Should return 400 with invalid fields")
		void shouldReturnBadRequestOnValidationFailure() throws Exception {
			UpdateUserProfileRequest badRequest = new UpdateUserProfileRequest();
			badRequest.setEmail("invalid-email");

			when(userService.updateUserProfile(eq(user.getId()), any()))
					.thenThrow(new IllegalArgumentException("Invalid email format"));

			mockMvc.perform(put("/users/me")
							.with(authenticated(userPrincipal))
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(badRequest)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(400))
					.andExpect(jsonPath("$.message").value("Invalid email format"));
		}
	}

}
