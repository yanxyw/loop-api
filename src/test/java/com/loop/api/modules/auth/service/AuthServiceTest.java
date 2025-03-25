package com.loop.api.modules.auth.service;

import com.loop.api.common.exception.UserAlreadyExistsException;
import com.loop.api.modules.auth.dto.RegisterRequest;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthService authService;

	@Nested
	@DisplayName("Tests for signup service")
	class RegisterTests {
		@Test
		@DisplayName("Should register user successfully")
		void shouldRegisterUserSuccessfully() {
			RegisterRequest request = new RegisterRequest("new@example.com", "password", "newuser");

			when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
			when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
			when(passwordEncoder.encode("password")).thenReturn("encodedPass");

			String result = authService.registerUser(request);

			assertEquals("User registered successfully", result);
			verify(userRepository).save(any(User.class));
		}

		@Test
		void shouldThrowIllegalArgumentExceptionForMissingFields() {
			RegisterRequest request = new RegisterRequest();

			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
					() -> authService.registerUser(request));

			assertEquals("Email, username, and password cannot be empty or null", ex.getMessage());
		}

		@Test
		void shouldThrowUserAlreadyExistsExceptionIfEmailExists() {
			RegisterRequest request = new RegisterRequest("exists@example.com", "password", "existinguser");

			User existingUser = new User();
			existingUser.setId(123L);

			when(userRepository.findByEmail("exists@example.com"))
					.thenReturn(Optional.of(existingUser));

			assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));
		}

		@Test
		void shouldThrowUserAlreadyExistsExceptionIfUsernameExists() {
			RegisterRequest request = new RegisterRequest("exists@example.com", "password", "existinguser");

			User existingUser = new User();
			existingUser.setId(123L);
			when(userRepository.findByEmail("exists@example.com")).thenReturn(Optional.empty());
			when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

			assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));
		}

		@Test
		void shouldThrowRuntimeExceptionOnUnexpectedSaveFailure() {
			RegisterRequest request = new RegisterRequest("new@example.com", "password", "newuser");
			request.setEmail("new@example.com");

			when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
			when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
			when(passwordEncoder.encode(any())).thenReturn("encoded");
			doThrow(new RuntimeException("DB error")).when(userRepository).save(any(User.class));

			RuntimeException ex = assertThrows(RuntimeException.class,
					() -> authService.registerUser(request));

			assertTrue(ex.getMessage().contains("Error registering user"));
		}


	}
}

