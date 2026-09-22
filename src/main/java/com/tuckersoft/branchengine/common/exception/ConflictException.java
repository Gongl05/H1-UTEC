package com.tuckersoft.branchengine.common.exception;

/** Se traduce en el GlobalExceptionHandler. */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensaje) {
        super(mensaje);
    }
}
