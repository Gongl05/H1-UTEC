package com.tuckersoft.branchengine.common.exception;

/** Se traduce en el GlobalExceptionHandler. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String mensaje) {
        super(mensaje);
    }
}
