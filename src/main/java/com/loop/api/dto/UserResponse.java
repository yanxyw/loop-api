package com.loop.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String email;
    private String mobile;
    private String username;
    private boolean admin;
    private String profileUrl;
}
