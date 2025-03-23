package com.loop.api.common.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponse<T> {
    private Status status;
    private int code;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(Status status, int code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(HttpStatus httpStatus, String message, T data) {
        return new ApiResponse<>(Status.SUCCESS, httpStatus.value(), message, data);
    }

    public static <T> ApiResponse<T> error(HttpStatus httpStatus, String message) {
        return new ApiResponse<>(Status.ERROR, httpStatus.value(), message, null);
    }
}