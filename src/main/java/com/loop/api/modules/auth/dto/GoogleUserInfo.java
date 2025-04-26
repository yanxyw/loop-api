package com.loop.api.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GoogleUserInfo {
	private String sub;
	private String email;
	private boolean emailVerified;
	private String name;
	private String picture;
}
