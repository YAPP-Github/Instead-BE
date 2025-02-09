package org.mainapp.global.response;

import java.time.LocalDateTime;

import org.mainapp.global.error.ErrorResponse;

public record GlobalResponse(int status, Object data, LocalDateTime timestamp) {
    public static GlobalResponse success(int status, Object data) {
        return new GlobalResponse(status, data, LocalDateTime.now());
    }

    public static GlobalResponse fail(int status, ErrorResponse errorResponse) {
        return new GlobalResponse(status, errorResponse, LocalDateTime.now());
    }
}
