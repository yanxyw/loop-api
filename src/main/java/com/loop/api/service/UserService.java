package com.loop.api.service;

import com.loop.api.dto.UserResponse;
import com.loop.api.exception.UserNotFoundException;
import com.loop.api.model.User;
import com.loop.api.repository.UserRepository;
import com.loop.api.util.UserValidationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public User createUser(User user) {
        UserValidationUtil.validateNewUser(user.getEmail(), user.getUsername(), user.getPassword(), user.getMobile(),
                userRepository);

        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User existingUser = getUserEntityById(id);
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setMobile(userDetails.getMobile());
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setPassword(userDetails.getPassword());
        existingUser.setAdmin(userDetails.isAdmin());
        existingUser.setProfileUrl(userDetails.getProfileUrl());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        userRepository.delete(user);
    }
}
