package com.loop.api.common.exception;

public class OAuthProcessingException extends RuntimeException {
    public OAuthProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}