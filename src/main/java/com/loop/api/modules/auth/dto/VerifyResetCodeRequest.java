package com.loop.api.modules.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyResetCodeRequest {
	private String email;
	private String code;
}
