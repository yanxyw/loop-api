package com.loop.api.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loop.api.common.constants.ApiRoutes;
import com.loop.api.common.exception.UserNotFoundException;
import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("UnitTest")
@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminUserControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	private List<UserResponse> mockUsers;

	@BeforeEach
	void setUp() {
		UserResponse user1 = new UserResponse(1L, "alice@example.com", "1234567890", "alice", false, "http://example" +
				".com/alice.png");
		UserResponse user2 = new UserResponse(2L, "bob@example.com", "0987654321", "bob", true, "http://example" +
				".com/bob.png"
		);
		mockUsers = List.of(user1, user2);
	}

	@Nested
	@DisplayName("Tests for get all users")
	class GetAllUsersTest {
		@Test
		@DisplayName("Should return 200 and list of all users for admin")
		void shouldReturnAllUsers() throws Exception {
			when(userService.getAllUsers()).thenReturn(mockUsers);

			mockMvc.perform(get(ApiRoutes.Admin.USERS)
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("SUCCESS"))
					.andExpect(jsonPath("$.code").value(200))
					.andExpect(jsonPath("$.message").value("Fetched all users"))
					.andExpect(jsonPath("$.data").isArray())
					.andExpect(jsonPath("$.data.length()").value(2))
					.andExpect(jsonPath("$.data[0].email").value("alice@example.com"))
					.andExpect(jsonPath("$.data[1].username").value("bob"));
		}

		@Test
		@DisplayName("Should return 200 and empty list if no users exist")
		void shouldReturnEmptyList() throws Exception {
			when(userService.getAllUsers()).thenReturn(List.of());

			mockMvc.perform(get(ApiRoutes.Admin.USERS)
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("SUCCESS"))
					.andExpect(jsonPath("$.code").value(200))
					.andExpect(jsonPath("$.data").isArray())
					.andExpect(jsonPath("$.data.length()").value(0));
		}
	}

	@Nested
	@DisplayName("Tests for get user by ID")
	class GetUserByIdTest {
		@Test
		@DisplayName("Should return 200 and user details when user exists")
		void shouldReturnUserById() throws Exception {
			UserResponse user = mockUsers.getFirst();
			Long userId = user.getId();

			when(userService.getUserById(userId)).thenReturn(user);

			mockMvc.perform(get(ApiRoutes.Admin.USERS + "/" + userId)
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("SUCCESS"))
					.andExpect(jsonPath("$.code").value(200))
					.andExpect(jsonPath("$.message").value("User fetched"))
					.andExpect(jsonPath("$.data.id").value(user.getId()))
					.andExpect(jsonPath("$.data.email").value(user.getEmail()))
					.andExpect(jsonPath("$.data.username").value(user.getUsername()))
					.andExpect(jsonPath("$.data.mobile").value(user.getMobile()))
					.andExpect(jsonPath("$.data.admin").value(user.isAdmin()))
					.andExpect(jsonPath("$.data.profileUrl").value(user.getProfileUrl()));
		}

		@Test
		@DisplayName("Should return 404 if user does not exist")
		void shouldReturnNotFoundIfUserMissing() throws Exception {
			Long missingId = 999L;
			when(userService.getUserById(missingId))
					.thenThrow(new UserNotFoundException("User not found with id: " + missingId));

			mockMvc.perform(get(ApiRoutes.Admin.USERS + "/" + missingId)
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(404))
					.andExpect(jsonPath("$.message").value("User not found with id: " + missingId));
		}
	}

	@Nested
	@DisplayName("Tests for delete user by ID")
	class DeleteUserByIdTest {

		@Test
		@DisplayName("Should delete user and return 200 when user exists")
		void shouldDeleteUserSuccessfully() throws Exception {
			Long userId = mockUsers.getFirst().getId();

			doNothing().when(userService).deleteUser(userId);

			mockMvc.perform(delete(ApiRoutes.Admin.USERS + "/" + userId)
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("SUCCESS"))
					.andExpect(jsonPath("$.code").value(200))
					.andExpect(jsonPath("$.message").value("User deleted successfully"))
					.andExpect(jsonPath("$.data").doesNotExist());
		}

		@Test
		@DisplayName("Should return 404 if user to delete does not exist")
		void shouldReturnNotFoundIfUserMissing() throws Exception {
			Long missingId = 999L;

			doThrow(new UserNotFoundException("User not found with id: " + missingId))
					.when(userService).deleteUser(missingId);

			mockMvc.perform(delete(ApiRoutes.Admin.USERS + "/" + missingId)
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(404))
					.andExpect(jsonPath("$.message").value("User not found with id: " + missingId));
		}
	}

}
