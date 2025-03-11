package com.loop.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileRequest {
    private String email;
    private String mobile;
    private String username;
    private Boolean admin;
    private String profileUrl;
}
