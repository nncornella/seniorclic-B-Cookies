package es.jlrn.configuration.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException {
//    
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());  // 404 Not Found
    }
}