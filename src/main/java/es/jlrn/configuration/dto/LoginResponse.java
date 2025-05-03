package es.jlrn.configuration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String message;  // Mensaje de éxito
    private String username; // Nombre de usuario
    private List<String> roles; // Roles asociados al usuario
    private String token; // El token JWT
}