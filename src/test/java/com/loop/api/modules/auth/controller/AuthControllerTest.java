package com.loop.api.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loop.api.common.exception.InvalidCredentialsException;
import com.loop.api.common.exception.UserAlreadyExistsException;
import com.loop.api.common.exception.UserNotFoundException;
import com.loop.api.modules.auth.dto.LoginRequest;
import com.loop.api.modules.auth.dto.LoginResponse;
import com.loop.api.modules.auth.dto.RegisterRequest;
import com.loop.api.modules.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("UnitTest")
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;

	@Nested
	@DisplayName("Tests for /signup")
	class RegisterTests {
		@Test
		@DisplayName("Signup: should register user successfully")
		void shouldRegisterUserSuccessfully() throws Exception {
			RegisterRequest request = new RegisterRequest("test@example.comr", "password", "newuser");

			when(authService.registerUser(any(RegisterRequest.class)))
					.thenReturn("User registered successfully");

			mockMvc.perform(post("/auth/signup")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.status").value("SUCCESS"))
					.andExpect(jsonPath("$.code").value(201))
					.andExpect(jsonPath("$.message").value("User registered"))
					.andExpect(jsonPath("$.data").value("User registered successfully"));
		}

		@Test
		@DisplayName("Should return 409 Conflict if user already exists")
		void shouldReturnConflictIfUserExists() throws Exception {
			RegisterRequest request = new RegisterRequest("exists@example.com", "password", "existinguser");

			when(authService.registerUser(any(RegisterRequest.class)))
					.thenThrow(new UserAlreadyExistsException("User with email 'exists@example.com' already exists."));

			mockMvc.perform(post("/auth/signup")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(409))
					.andExpect(jsonPath("$.message").value("User with email 'exists@example.com' already exists."));
		}

		@Test
		@DisplayName("Should return 400 Bad Request if fields are missing")
		void shouldReturnBadRequestForMissingFields() throws Exception {
			RegisterRequest request = new RegisterRequest(); // all fields null

			mockMvc.perform(post("/auth/signup")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(400))
					.andExpect(jsonPath("$.message").value("Validation failed"))
					.andExpect(jsonPath("$.data.email").value("Email is required"))
					.andExpect(jsonPath("$.data.username").value("Username is required"))
					.andExpect(jsonPath("$.data.password").value("Password is required"));
		}

		@ParameterizedTest
		@ValueSource(strings = {"not-an-email", "user@invalid", "a@b", "user@.com"})
		@DisplayName("Should return 400 for invalid email format")
		void shouldFailForInvalidEmail(String invalidEmail) throws Exception {
			RegisterRequest req = new RegisterRequest(invalidEmail, "password123", "validUsername");

			mockMvc.perform(post("/auth/signup")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(req)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(400))
					.andExpect(jsonPath("$.message").value("Validation failed"))
					.andExpect(jsonPath("$.data.email").value("Email format is invalid"));
		}

		@Test
		@DisplayName("Should return 400 for short password")
		void shouldFailForShortPassword() throws Exception {
			RegisterRequest req = new RegisterRequest("user@example.com", "123", "validUsername");

			mockMvc.perform(post("/auth/signup")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(req)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(400))
					.andExpect(jsonPath("$.message").value("Validation failed"))
					.andExpect(jsonPath("$.data.password").value("Password must be at least 8 characters"));
		}

		@Test
		@DisplayName("Should return 400 for invalid username characters")
		void shouldFailForInvalidUsername() throws Exception {
			RegisterRequest req = new RegisterRequest("user@example.com", "password123", "<script>");

			mockMvc.perform(post("/auth/signup")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(req)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(400))
					.andExpect(jsonPath("$.message").value("Validation failed"))
					.andExpect(jsonPath("$.data.username").value("Username can only contain letters, numbers and " +
							"underscores"));
		}

		@Test
		@DisplayName("Should return 500 if unexpected error occurs")
		void shouldReturnInternalServerErrorForUnexpectedError() throws Exception {
			RegisterRequest request = new RegisterRequest("new@example.com", "password", "newuser");

			when(authService.registerUser(any(RegisterRequest.class)))
					.thenThrow(new RuntimeException("An unexpected error occurred"));

			mockMvc.perform(post("/auth/signup")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(500))
					.andExpect(jsonPath("$.message").value("An unexpected error occurred"));
		}
	}

	@Nested
	@DisplayName("Tests for /login")
	class LoginTests {
		@Test
		@DisplayName("Login: should authenticate user and return token")
		void shouldLoginSuccessfully() throws Exception {
			LoginRequest loginRequest = new LoginRequest("test@example.com", "test1234");
			LoginResponse loginResponse = new LoginResponse("abc123");

			when(authService.loginUser(any(LoginRequest.class)))
					.thenReturn(loginResponse);

			mockMvc.perform(post("/auth/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(loginRequest)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value("SUCCESS"))
					.andExpect(jsonPath("$.code").value(200))
					.andExpect(jsonPath("$.message").value("Login successful"))
					.andExpect(jsonPath("$.data.token").value("abc123"));
		}

		@Test
		@DisplayName("Should return 400 if email or password is missing")
		void shouldReturnBadRequestIfFieldsAreMissing() throws Exception {
			LoginRequest request = new LoginRequest("invalid", null);

			mockMvc.perform(post("/auth/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(400))
					.andExpect(jsonPath("$.message").value("Validation failed"))
					.andExpect(jsonPath("$.data.email").value("Email format is invalid"))
					.andExpect(jsonPath("$.data.password").value("Password is required"));
		}

		@Test
		@DisplayName("Should return 400 if login request is missing fields")
		void shouldFailLoginValidation() throws Exception {
			LoginRequest req = new LoginRequest("", "123");

			mockMvc.perform(post("/auth/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(req)))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.data.email").value("Email is required"))
					.andExpect(jsonPath("$.data.password").value("Password must be at least 8 characters"));
		}

		@Test
		@DisplayName("Should return 404 if user not found")
		void shouldReturnNotFoundIfUserNotFound() throws Exception {
			LoginRequest request = new LoginRequest("unknown@example.com", "password");

			when(authService.loginUser(any(LoginRequest.class)))
					.thenThrow(new UserNotFoundException("No account found with this email"));

			mockMvc.perform(post("/auth/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(404))
					.andExpect(jsonPath("$.message").value("No account found with this email"));
		}

		@Test
		@DisplayName("Should return 401 if password is incorrect")
		void shouldReturnUnauthorizedIfPasswordIncorrect() throws Exception {
			LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

			when(authService.loginUser(any(LoginRequest.class)))
					.thenThrow(new InvalidCredentialsException("Invalid email or password"));

			mockMvc.perform(post("/auth/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(401))
					.andExpect(jsonPath("$.message").value("Invalid email or password"));
		}

		@Test
		@DisplayName("Should return 500 if unexpected error occurs")
		void shouldReturnInternalServerErrorWhenUnexpectedExceptionOccurs() throws Exception {
			LoginRequest request = new LoginRequest("test@example.com", "password");

			when(authService.loginUser(any(LoginRequest.class)))
					.thenThrow(new RuntimeException("An unexpected error occurred"));

			mockMvc.perform(post("/auth/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.status").value("ERROR"))
					.andExpect(jsonPath("$.code").value(500))
					.andExpect(jsonPath("$.message").value("An unexpected error occurred"));
		}
	}
}