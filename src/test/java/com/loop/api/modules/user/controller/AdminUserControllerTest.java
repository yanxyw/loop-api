package com.loop.api.modules.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loop.api.common.constants.ApiRoutes;
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

import static org.mockito.Mockito.when;
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
}
