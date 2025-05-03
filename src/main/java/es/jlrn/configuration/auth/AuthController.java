package es.jlrn.configuration.auth;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.jlrn.configuration.dto.AuthRequestDTO;
import es.jlrn.configuration.dto.LoginResponse;
import es.jlrn.configuration.dto.RegisterRequestDTO;
import es.jlrn.configuration.jwt.JwtService;
import es.jlrn.persistence.model.UserEntity;
import es.jlrn.persistence.repositories.UserRepository;
import es.jlrn.presentation.messages.ApiMessage;
import es.jlrn.service.impl.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// AuthController.java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
//
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserServiceImpl userService;
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request, HttpServletResponse response) {
        try {
            // Autenticar al usuario con el nombre de usuario y la contraseña
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Obtener los detalles del usuario autenticado
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Extraer los roles del usuario autenticado
            List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

            // Generar el token JWT
            String jwt = jwtService.generateTokenWithRoles(userDetails, roles);

            // Establecer la cookie de JWT
            setJwtCookie(response, jwt);

            // Crear la respuesta con el mensaje de éxito, el nombre de usuario, los roles y el token JWT
            LoginResponse loginResponse = new LoginResponse("Login exitoso", userDetails.getUsername(), roles, jwt);
            return ResponseEntity.ok(loginResponse);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body( new ApiMessage("Credenciales inválidas"));
        } catch (Exception e) {
            // Capturar cualquier otro error y dar un mensaje genérico
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiMessage("Error interno del servidor"));
        }
    }

    // Método para establecer la cookie JWT en la respuesta
    private void setJwtCookie(HttpServletResponse response, String jwt) {
        // Establecer la cookie JWT
        Cookie cookie = new Cookie("jwt", jwt);  // "jwt" es el nombre de la cookie
        cookie.setHttpOnly(true);  // Evitar que JavaScript acceda a la cookie
        cookie.setSecure(true);    // Asegurarse de que la cookie solo se envíe a través de HTTPS
        cookie.setPath("/");       // La cookie estará disponible para todo el dominio
        cookie.setMaxAge(24 * 60 * 60); // La cookie expira en 1 día (24 horas)

        // Establecer la cookie en la respuesta
        response.addCookie(cookie);
    }

    // Método para obtener el JWT de las cookies
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();  // Retorna el valor de la cookie JWT
                }
            }
        }
        return null;  // Si no se encuentra la cookie
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        String expiredCookie = "jwt=; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=0";
        response.setHeader("Set-Cookie", expiredCookie);

        return ResponseEntity.ok(Map.of("message",  new ApiMessage("Logout exitoso")));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request, HttpServletResponse response) {
        String jwt = authService.register(request);

        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new ApiMessage("Usuario registrado correctamente"));
    }

    // Método para refrescar el token JWT
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestHeader("Authorization") String authHeader) {
        // El token debería llegar como: "Bearer eyJhbGciOiJIUzI1NiIs..."
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token inválido o faltante");
        }

        String token = authHeader.substring(7); // Remueve "Bearer "
        try {
            String username = jwtService.getUsernameFromToken(token);
            UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            String newToken = jwtService.generateToken(user);
            return ResponseEntity.ok(newToken);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No se pudo refrescar el token");
        }
    }

    @GetMapping("/check-auth")
    public ResponseEntity<String> checkAuth() {
        return ResponseEntity.ok().body("Autenticado");
    }

    @GetMapping("/user/details")
    public ResponseEntity<UserEntity> getAuthenticatedUser() {
        UserEntity user = userService.getUserDetails();
        return ResponseEntity.ok(user);
    }
}