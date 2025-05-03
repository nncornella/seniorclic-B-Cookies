package es.jlrn.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.jlrn.persistence.model.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
//    
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    UserEntity findByIdAndActivoTrue(Long id);  
    //
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}