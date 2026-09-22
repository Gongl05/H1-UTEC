package com.tuckersoft.branchengine.common;

import java.time.Instant;

/** Formato unico de error del enunciado: { error, message, timestamp, path }. */
public record ApiErrorResponse(String error, String message, Instant timestamp, String path) {

    public static ApiErrorResponse of(String error, String message, String path) {
        return new ApiErrorResponse(error, message, Instant.now(), path);
    }
}
