package com.loop.api.modules.user.mapper;

import com.loop.api.modules.user.dto.UserResponse;
import com.loop.api.modules.user.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setMobile(user.getMobile());
        response.setUsername(user.getUsername());
        response.setAdmin(user.isAdmin());
        response.setProfileUrl(user.getProfileUrl());
        return response;
    }

    public List<UserResponse> toUserResponseList(List<User> users) {
        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }
}
