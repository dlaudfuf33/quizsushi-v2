package com.cmdlee.quizsushi.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final String code;
    private final String message;
    private final int status;
    private final LocalDateTime timestamp;

    public static ErrorResponse from(ErrorCode code) {
        return ErrorResponse.builder()
                .code(code.getCode())
                .message(code.getMessage())
                .status(code.getStatus().value())
                .timestamp(LocalDateTime.now())
                .build();
    }
}