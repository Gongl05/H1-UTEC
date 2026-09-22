package com.tuckersoft.branchengine.common.exception;

/** Se traduce en el GlobalExceptionHandler. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String mensaje) {
        super(mensaje);
    }
}
