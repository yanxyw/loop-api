package com.loop.api.common.dto.response;

import org.springframework.http.HttpStatus;

public record StandardResponse<T>(Status status, int code, String message, T data) {

	public static <T> StandardResponse<T> success(HttpStatus httpStatus, String message, T data) {
		return new StandardResponse<>(Status.SUCCESS, httpStatus.value(), message, data);
	}

	public static <T> StandardResponse<T> error(HttpStatus status, String message, T data) {
		return new StandardResponse<>(Status.ERROR, status.value(), message, data);
	}

	public static <T> StandardResponse<T> error(HttpStatus httpStatus, String message) {
		return new StandardResponse<>(Status.ERROR, httpStatus.value(), message, null);
	}
}