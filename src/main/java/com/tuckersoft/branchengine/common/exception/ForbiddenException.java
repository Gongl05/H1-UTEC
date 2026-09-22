package com.tuckersoft.branchengine.common.exception;

/** Se traduce en el GlobalExceptionHandler. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String mensaje) {
        super(mensaje);
    }
}
