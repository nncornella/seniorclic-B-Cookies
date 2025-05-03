package es.jlrn.service.interfaces;

import org.springframework.security.core.userdetails.UserDetails;

import es.jlrn.persistence.model.UserEntity;

public interface IUserService {
//
    UserDetails loadUserByUsername(String userName);
    UserEntity findByUserName(String userName);
    boolean existsByUserName(String username);
    void save(UserEntity user);
    UserEntity getUserDetails();
}
