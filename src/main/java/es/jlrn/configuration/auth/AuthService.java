package es.jlrn.configuration.auth;

import java.util.Set;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.jlrn.configuration.dto.LoginRequestDTO;
import es.jlrn.configuration.dto.RegisterRequestDTO;
import es.jlrn.configuration.jwt.JwtService;
import es.jlrn.persistence.enums.ListRoles;
import es.jlrn.persistence.model.RoleEntity;
import es.jlrn.persistence.model.UserEntity;
import es.jlrn.persistence.repositories.RoleRepository;
import es.jlrn.persistence.repositories.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
//
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    //

    public String login(LoginRequestDTO request) {
    //    
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        return jwtService.generateToken(user);
    }

    // Método para establecer la cookie JWT en la respuesta
    public void logout(HttpServletResponse response) {
        String expiredCookie = "jwt=; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=0";
        response.setHeader("Set-Cookie", expiredCookie);
    }

    // Método para registrar un nuevo usuario 
    public String register(RegisterRequestDTO request) {
    //    
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }

        RoleEntity userRole = roleRepository.findByName(ListRoles.ROLE_USER)
            .orElseThrow(() -> new RuntimeException("Rol ROLE_USER no encontrado"));

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .roles(Set.of(userRole))
                .activo(true)
                .build();

        userRepository.save(user);

        return jwtService.generateToken(user);
    }

    // Método para refrescar el token JWT
    public String refreshToken(String token) {
        String username = jwtService.getUsernameFromToken(token);
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return jwtService.generateToken(user);
    }
}
