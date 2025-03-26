package com.loop.api.modules.user.service;

import com.loop.api.common.exception.UserNotFoundException;
import com.loop.api.common.util.UserValidationUtil;
import com.loop.api.modules.user.dto.UpdateUserProfileRequest;
import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.mapper.UserMapper;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public UserService(UserRepository userRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	public List<UserResponse> getAllUsers() {
		List<User> users = userRepository.findAll();
		return userMapper.toUserResponseList(users);
	}

	public UserResponse getUserById(Long id) {
		User user = getUserEntityById(id);
		return userMapper.toUserResponse(user);
	}

	public User getUserEntityById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
	}

	public UserResponse updateUserProfile(Long id, UpdateUserProfileRequest profileRequest) {
		User existingUser = getUserEntityById(id);

		UserValidationUtil.validateUniqueUserFields(
				profileRequest.getEmail(),
				profileRequest.getUsername(),
				profileRequest.getMobile(),
				userRepository,
				id
		);

		if (profileRequest.getEmail() != null)
			existingUser.setEmail(profileRequest.getEmail());
		if (profileRequest.getMobile() != null)
			existingUser.setMobile(profileRequest.getMobile());
		if (profileRequest.getUsername() != null)
			existingUser.setUsername(profileRequest.getUsername());
		if (profileRequest.getProfileUrl() != null)
			existingUser.setProfileUrl(profileRequest.getProfileUrl());

		return userMapper.toUserResponse(userRepository.save(existingUser));
	}

	public void deleteUser(Long id) {
		User user = getUserEntityById(id);
		userRepository.delete(user);
	}
}

