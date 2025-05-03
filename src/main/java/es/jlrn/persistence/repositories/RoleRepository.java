package es.jlrn.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.jlrn.persistence.enums.ListRoles;
import es.jlrn.persistence.model.RoleEntity;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
//    
    Optional<RoleEntity> findByName(ListRoles name);
    Optional<RoleEntity> findByName(String name);
    //
    boolean existsById(Long id);
    boolean existsByName(ListRoles name);
}
