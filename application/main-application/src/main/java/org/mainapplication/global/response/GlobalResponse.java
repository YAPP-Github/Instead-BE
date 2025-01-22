package org.mainapplication.global.response;

import java.time.LocalDateTime;

import org.mainapplication.global.error.ErrorResponse;

public record GlobalResponse(boolean success, int status, Object data, LocalDateTime timestamp) {
    public static GlobalResponse success(int status, Object data) {
        return new GlobalResponse(true, status, data, LocalDateTime.now());
    }

    public static GlobalResponse fail(int status, ErrorResponse errorResponse) {
        return new GlobalResponse(false, status, errorResponse, LocalDateTime.now());
    }
}
