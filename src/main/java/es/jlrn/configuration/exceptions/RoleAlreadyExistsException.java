package es.jlrn.configuration.exceptions;

import org.springframework.http.HttpStatus;

public class RoleAlreadyExistsException extends AppException {
    public RoleAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }
}

