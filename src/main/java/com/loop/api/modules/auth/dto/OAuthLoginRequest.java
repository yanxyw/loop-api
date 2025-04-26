package com.loop.api.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OAuthLoginRequest {
	private String provider;
	private String code;
	private String redirectUri;
}
