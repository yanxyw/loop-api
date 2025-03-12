package com.loop.api.service;

import com.loop.api.dto.UpdateUserProfileRequest;
import com.loop.api.dto.UserResponse;
import com.loop.api.exception.UserNotFoundException;
import com.loop.api.model.User;
import com.loop.api.repository.UserRepository;
import com.loop.api.util.UserValidationUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = getUserEntityById(id);
        return convertToUserResponse(user);
    }

    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    // Private helper function to map User to UserResponse DTO
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setMobile(user.getMobile());
        response.setUsername(user.getUsername());
        response.setAdmin(user.isAdmin());
        response.setProfileUrl(user.getProfileUrl());
        return response;
    }

    public UserResponse createUser(User user) {
        UserValidationUtil.validateNewUser(user.getEmail(), user.getUsername(), user.getPassword(), user.getMobile(),
                userRepository);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return convertToUserResponse(userRepository.save(user));
    }

    public UserResponse updateUserProfile(Long id, UpdateUserProfileRequest profileRequest) {
        User existingUser = getUserEntityById(id);

        UserValidationUtil.validateUniqueUserFields(
                profileRequest.getEmail(),
                profileRequest.getUsername(),
                profileRequest.getMobile(),
                userRepository,
                id);

        if (profileRequest.getEmail() != null) {
            existingUser.setEmail(profileRequest.getEmail());
        }
        if (profileRequest.getMobile() != null) {
            existingUser.setMobile(profileRequest.getMobile());
        }
        if (profileRequest.getUsername() != null) {
            existingUser.setUsername(profileRequest.getUsername());
        }
        if (profileRequest.getProfileUrl() != null) {
            existingUser.setProfileUrl(profileRequest.getProfileUrl());
        }
        return convertToUserResponse(userRepository.save(existingUser));
    }

    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        userRepository.delete(user);
    }
}
