package es.jlrn.configuration.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private String details;
    private LocalDateTime timestamp;
    private int status;

    // Solo este constructor debería ser necesario
    public ErrorResponse(String message, String details, int status) {
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
        this.status = status;
    }
}

