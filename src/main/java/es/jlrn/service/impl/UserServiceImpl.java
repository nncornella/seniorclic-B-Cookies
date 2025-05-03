package es.jlrn.service.impl;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import es.jlrn.persistence.model.UserEntity;
import es.jlrn.persistence.repositories.UserRepository;
import es.jlrn.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
//
    private final UserRepository userRepository;   

    // Implementación de los métodos de IUserService
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
    //    
        UserEntity user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                user.getRoles().iterator().next().getName().toString()
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(authority)
        );
    }

    @Override
    public UserEntity findByUserName(String userName) {
         return userRepository.findByUsername(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public boolean existsByUserName(String username) {
        // Lógica para verificar si un usuario existe por nombre de usuario
        return userRepository.existsByUsername(username);
    }

    @Override
    public void save(UserEntity user) {
        // Lógica para guardar un usuario
        userRepository.save(user);
    }

    @Override
    public UserEntity getUserDetails() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUserName(username);
    }
}
