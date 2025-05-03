package es.jlrn.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.jlrn.persistence.enums.ListPermission;
import es.jlrn.persistence.model.PermissionsEntity;

@Repository
public interface PermissionsRepository extends JpaRepository<PermissionsEntity, Long> {
//   
    Optional<PermissionsEntity> findById(Long id);
    Optional<PermissionsEntity> findByName(ListPermission name);

    boolean existsByName(String name);
    boolean existsById(Long id);
    boolean existsByName(ListPermission name);

    Optional<PermissionsEntity> findByName(String name);

    List<PermissionsEntity> findByNameIn(List<String> asList);
}
