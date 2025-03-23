package com.loop.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileRequest {
    private String email;
    private String mobile;
    private String username;
    private String profileUrl;
}
