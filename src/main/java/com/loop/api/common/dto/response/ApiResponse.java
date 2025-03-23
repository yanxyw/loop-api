package com.loop.api.common.dto.response;

import lombok.Getter;

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

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(Status.SUCCESS, 200, message, data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(Status.ERROR, code, message, null);
    }
}

