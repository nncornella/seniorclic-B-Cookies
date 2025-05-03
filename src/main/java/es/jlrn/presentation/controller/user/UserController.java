package es.jlrn.presentation.controller.user;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.jlrn.persistence.model.UserEntity;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
//
    //private final UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        UserEntity user = (UserEntity) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of(
            "username", user.getUsername(),
            "email", user.getEmail(),
            "roles", user.getRoles().stream().map(r -> r.getName().name()).toList(),
            "permissions", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
        ));
    }
}
