package es.jlrn.configuration.exceptions;

import org.springframework.http.HttpStatus;

public class PermissionAlreadyExistsException extends AppException {
    public PermissionAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }
}