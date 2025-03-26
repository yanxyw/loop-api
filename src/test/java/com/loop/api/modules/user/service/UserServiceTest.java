package com.loop.api.modules.user.service;

import com.loop.api.common.exception.UserAlreadyExistsException;
import com.loop.api.common.exception.UserNotFoundException;
import com.loop.api.modules.user.dto.UpdateUserProfileRequest;
import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.mapper.UserMapper;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import com.loop.api.testutils.TestUserFactory;
import com.loop.api.testutils.TestUserResponseFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserMapper userMapper;

	@InjectMocks
	private UserService userService;

	private List<User> mockUsers;
	private List<UserResponse> mockResponses;

	private UserResponse response1;
	private UserResponse response2;

	@BeforeEach
	void setUp() {
		User user1 = TestUserFactory.regularUser(1L);
		User user2 = TestUserFactory.adminUser(2L);
		mockUsers = List.of(user1, user2);

		response1 = TestUserResponseFactory.fromUser(user1);
		response2 = TestUserResponseFactory.fromUser(user2);
		mockResponses = List.of(response1, response2);
	}

	@Nested
	@DisplayName("Tests for get all users")
	class GetAllUsersTest {
		@Test
		@DisplayName("Should return list of all users")
		void shouldReturnAllUsers() {
			when(userRepository.findAll()).thenReturn(mockUsers);
			when(userMapper.toUserResponseList(mockUsers)).thenReturn(mockResponses);

			List<UserResponse> result = userService.getAllUsers();

			assertEquals(2, result.size());
			assertEquals(response1.getEmail(), result.get(0).getEmail());
			assertEquals(response2.getUsername(), result.get(1).getUsername());

			verify(userRepository).findAll();
			verify(userMapper).toUserResponseList(mockUsers);
		}
	}

	@Nested
	@DisplayName("Tests for get user by ID")
	class GetUserByIdTests {
		@Test
		@DisplayName("Should return user response when user exists")
		void shouldReturnUserResponseById() {
			User user = TestUserFactory.regularUser(1L);
			UserResponse expectedResponse = TestUserResponseFactory.fromUser(user);

			when(userRepository.findById(1L)).thenReturn(Optional.of(user));
			when(userMapper.toUserResponse(user)).thenReturn(expectedResponse);

			UserResponse result = userService.getUserById(1L);

			assertNotNull(result);
			assertEquals(expectedResponse.getEmail(), result.getEmail());
			assertEquals(expectedResponse.getUsername(), result.getUsername());

			verify(userRepository).findById(1L);
			verify(userMapper).toUserResponse(user);
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when user does not exist")
		void shouldThrowWhenUserNotFound() {
			Long missingId = 999L;
			when(userRepository.findById(missingId)).thenReturn(Optional.empty());

			UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> {
				userService.getUserById(missingId);
			});

			assertEquals("User not found with id: 999", ex.getMessage());
			verify(userRepository).findById(missingId);
			verifyNoInteractions(userMapper);
		}
	}

	@Nested
	@DisplayName("Tests for update user profile")
	class UpdateUserProfileTests {

		@Test
		@DisplayName("Should update user profile and return updated response")
		void shouldUpdateUserProfileSuccessfully() {
			User existingUser = TestUserFactory.regularUser(1L);
			UpdateUserProfileRequest request = new UpdateUserProfileRequest();
			request.setEmail("new@example.com");
			request.setMobile("9876543210");
			request.setUsername("newUsername");
			request.setProfileUrl("http://example.com/new.png");

			User updatedUser = TestUserFactory.regularUser(1L);
			updatedUser.setEmail(request.getEmail());
			updatedUser.setMobile(request.getMobile());
			updatedUser.setUsername(request.getUsername());
			updatedUser.setProfileUrl(request.getProfileUrl());

			UserResponse expectedResponse = TestUserResponseFactory.fromUser(updatedUser);

			when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
			when(userRepository.save(any(User.class))).thenReturn(updatedUser);
			when(userMapper.toUserResponse(updatedUser)).thenReturn(expectedResponse);

			UserResponse result = userService.updateUserProfile(1L, request);

			assertEquals(expectedResponse.getEmail(), result.getEmail());
			assertEquals(expectedResponse.getUsername(), result.getUsername());
			assertEquals(expectedResponse.getProfileUrl(), result.getProfileUrl());

			verify(userRepository).findById(1L);
			verify(userRepository).save(existingUser);
			verify(userMapper).toUserResponse(updatedUser);
		}

		@Test
		@DisplayName("Should update only email when other fields are null")
		void shouldUpdateOnlyEmail() {
			User existingUser = TestUserFactory.regularUser(1L);
			UpdateUserProfileRequest request = new UpdateUserProfileRequest();
			request.setEmail("updated@example.com");

			User updatedUser = TestUserFactory.regularUser(1L);
			updatedUser.setEmail(request.getEmail());

			UserResponse expected = TestUserResponseFactory.fromUser(updatedUser);

			when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
			when(userRepository.save(any(User.class))).thenReturn(updatedUser);
			when(userMapper.toUserResponse(updatedUser)).thenReturn(expected);

			UserResponse result = userService.updateUserProfile(1L, request);

			assertEquals(expected.getEmail(), result.getEmail());
			verify(userRepository).save(existingUser);
		}

		@Test
		@DisplayName("Should skip updates if all fields are null")
		void shouldNotUpdateIfNoFieldsProvided() {
			User existingUser = TestUserFactory.regularUser(1L);
			UpdateUserProfileRequest request = new UpdateUserProfileRequest();

			UserResponse expected = TestUserResponseFactory.fromUser(existingUser);

			when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
			when(userRepository.save(any(User.class))).thenReturn(existingUser);
			when(userMapper.toUserResponse(existingUser)).thenReturn(expected);

			UserResponse result = userService.updateUserProfile(1L, request);

			assertEquals(expected.getEmail(), result.getEmail());
			assertEquals(expected.getUsername(), result.getUsername());
			verify(userRepository).save(existingUser);
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when user does not exist")
		void shouldThrowIfUserNotFound() {
			when(userRepository.findById(1L)).thenReturn(Optional.empty());

			UpdateUserProfileRequest request = new UpdateUserProfileRequest();
			request.setEmail("new@example.com");

			assertThrows(UserNotFoundException.class, () -> {
				userService.updateUserProfile(1L, request);
			});

			verify(userRepository).findById(1L);
			verify(userRepository, never()).save(any());
			verifyNoInteractions(userMapper);
		}

		@Test
		@DisplayName("Should throw UserAlreadyExistsException if email/username/mobile is not unique")
		void shouldThrowIfDuplicateFieldExists() {
			User existingUser = TestUserFactory.regularUser(1L);

			UpdateUserProfileRequest request = new UpdateUserProfileRequest();
			request.setEmail("duplicate@example.com");

			when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

			doThrow(new UserAlreadyExistsException("User with email 'duplicate@example.com' already exists."))
					.when(userRepository).findByEmail("duplicate@example.com");

			assertThrows(UserAlreadyExistsException.class, () -> {
				userService.updateUserProfile(1L, request);
			});

			verify(userRepository).findById(1L);
			verify(userRepository, never()).save(any());
			verifyNoInteractions(userMapper);
		}
	}

	@Nested
	@DisplayName("Tests for delete user")
	class DeleteUserTests {

		@Test
		@DisplayName("Should delete user when user exists")
		void shouldDeleteUserSuccessfully() {
			User existingUser = TestUserFactory.regularUser(1L);
			when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
			doNothing().when(userRepository).delete(existingUser);

			userService.deleteUser(1L);

			verify(userRepository).findById(1L);
			verify(userRepository).delete(existingUser);
		}

		@Test
		@DisplayName("Should throw UserNotFoundException when user does not exist")
		void shouldThrowWhenUserNotFound() {
			when(userRepository.findById(1L)).thenReturn(Optional.empty());

			assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));

			verify(userRepository).findById(1L);
			verify(userRepository, never()).delete(any());
		}
	}
}