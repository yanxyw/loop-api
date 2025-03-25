package com.loop.api.modules.auth.service;

import com.loop.api.common.exception.InvalidCredentialsException;
import com.loop.api.common.exception.UserAlreadyExistsException;
import com.loop.api.common.exception.UserNotFoundException;
import com.loop.api.modules.auth.dto.LoginRequest;
import com.loop.api.modules.auth.dto.LoginResponse;
import com.loop.api.modules.auth.dto.RegisterRequest;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import com.loop.api.security.JwtTokenProvider;
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
	@Mock
	private JwtTokenProvider jwtTokenProvider;

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
		@DisplayName("Should throw UserAlreadyExistsException when email already exists")
		void shouldThrowUserAlreadyExistsExceptionIfEmailExists() {
			RegisterRequest request = new RegisterRequest("exists@example.com", "password", "existinguser");

			User existingUser = new User();
			existingUser.setId(123L);

			when(userRepository.findByEmail("exists@example.com"))
					.thenReturn(Optional.of(existingUser));

			assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));
		}

		@Test
		@DisplayName("Should throw UserAlreadyExistsException when username already exists")
		void shouldThrowUserAlreadyExistsExceptionIfUsernameExists() {
			RegisterRequest request = new RegisterRequest("exists@example.com", "password", "existinguser");

			User existingUser = new User();
			existingUser.setId(123L);
			when(userRepository.findByEmail("exists@example.com")).thenReturn(Optional.empty());
			when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

			assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));
		}

		@Test
		@DisplayName("Should throw RuntimeException when saving user fails unexpectedly")
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

		@Nested
		@DisplayName("Tests for login service")
		class LoginTests {
			@Test
			@DisplayName("Should return token when login is successful")
			void shouldLoginSuccessfully() {
				LoginRequest request = new LoginRequest("user@example.com", "password");
				User user = new User();
				user.setEmail("user@example.com");
				user.setPassword("hashedPassword");

				when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
				when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
				when(jwtTokenProvider.generateToken("user@example.com")).thenReturn("jwt-token");

				LoginResponse response = authService.loginUser(request);

				assertEquals("jwt-token", response.getToken());
				verify(userRepository).findByEmail("user@example.com");
				verify(passwordEncoder).matches("password", "hashedPassword");
				verify(jwtTokenProvider).generateToken("user@example.com");
			}

			@Test
			@DisplayName("Should throw UserNotFoundException if user does not exist")
			void shouldThrowIfUserNotFound() {
				LoginRequest request = new LoginRequest("notfound@example.com", "password");

				when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

				assertThrows(UserNotFoundException.class, () -> authService.loginUser(request));
			}

			@Test
			@DisplayName("Should throw InvalidCredentialsException if password is incorrect")
			void shouldThrowIfPasswordIncorrect() {
				LoginRequest request = new LoginRequest("user@example.com", "wrongPassword");
				User user = new User();
				user.setEmail("user@example.com");
				user.setPassword("hashedPassword");

				when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
				when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

				assertThrows(InvalidCredentialsException.class, () -> authService.loginUser(request));
			}

			@Test
			@DisplayName("Should throw RuntimeException if unexpected exception occurs")
			void shouldThrowRuntimeExceptionOnUnexpectedError() {
				LoginRequest request = new LoginRequest("user@example.com", "password");

				when(userRepository.findByEmail("user@example.com")).thenThrow(new RuntimeException("DB error"));

				RuntimeException exception = assertThrows(RuntimeException.class,
						() -> authService.loginUser(request));
				assertTrue(exception.getMessage().contains("Error during login: DB error"));
			}
		}
	}
}

