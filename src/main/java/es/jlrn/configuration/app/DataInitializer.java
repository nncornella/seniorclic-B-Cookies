package es.jlrn.configuration.app;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import es.jlrn.persistence.enums.ListPermission;
import es.jlrn.persistence.enums.ListRoles;
import es.jlrn.persistence.model.PermissionsEntity;
import es.jlrn.persistence.model.RoleEntity;
import es.jlrn.persistence.model.UserEntity;
import es.jlrn.persistence.repositories.PermissionsRepository;
import es.jlrn.persistence.repositories.RoleRepository;
import es.jlrn.persistence.repositories.UserRepository;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.init-data", havingValue = "true")
public class DataInitializer {
//
    private final RoleRepository roleRepository;
    private final PermissionsRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void init() {
        System.out.println("🚀 Ejecutando DataInitializer...");

        // Crear permisos si no existen
        for (ListPermission permissionEnum : ListPermission.values()) {
            if (!permissionRepository.existsByName(permissionEnum)) {
                PermissionsEntity permission = PermissionsEntity.builder()
                        .name(permissionEnum)
                        .build();
                permissionRepository.save(permission);
                System.out.println("✅ Permiso creado: " + permissionEnum.name());
            } else {
                System.out.println("ℹ️ Permiso ya existe: " + permissionEnum.name());
            }
        }

        // Crear roles y asignar permisos por defecto
        for (ListRoles roleEnum : ListRoles.values()) {
            System.out.println("🔍 Verificando si el rol existe: " + roleEnum.name());

            // Verificar si el rol ya existe en la base de datos
            boolean exists = roleRepository.existsByName(roleEnum);
            if (!exists) {
                try {
                    // Si no existe, crear el rol
                    RoleEntity role = RoleEntity.builder()
                            .name(roleEnum)
                            .permissions(obtenerPermisosPorRol(roleEnum))  // Asignar permisos según el rol
                            .build();
                    roleRepository.save(role);
                    System.out.println("✅ Rol creado: " + roleEnum.name());
                } catch (Exception e) {
                    // Capturar cualquier error durante la creación del rol
                    System.out.println("❌ Error al crear el rol " + roleEnum.name() + ": " + e.getMessage());
                }
            } else {
                System.out.println("ℹ️ Rol ya existe: " + roleEnum.name());
            }
        }

        // Crear usuario por defecto
        crearUsuarioPorDefecto();

        // Mostrar todos los roles en DB
        System.out.println("📋 Todos los roles en DB:");
        roleRepository.findAll().forEach(role ->
                System.out.println("🔹 " + role.getName()));
    }

    // Método para crear un usuario por defecto
    private void crearUsuarioPorDefecto() {
        String username = "pnry";
        String email = "nn@gmail.com";
        String password = "12345678"; // Deberías encriptar la contraseña en producción

        // Verificar si el usuario ya existe
        boolean userExists = userRepository.existsByUsername(username);
        if (!userExists) {
            // Crear usuario por defecto
            UserEntity user = UserEntity.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password)) // Encriptar la contraseña
                    .activo(true)
                    .roles(obtenerRolesPorDefecto()) // Asignar roles al usuario
                    .build();
            userRepository.save(user);
            System.out.println("✅ Usuario por defecto creado: " + username);
        } else {
            System.out.println("ℹ️ Usuario por defecto ya existe: " + username);
        }
    }

    // Método auxiliar para obtener los roles predeterminados para el usuario por defecto
    private Set<RoleEntity> obtenerRolesPorDefecto() {
        Set<RoleEntity> rolesSet = new HashSet<>();

        // Asignar roles al usuario por defecto. Puedes modificar según tus necesidades.
        RoleEntity superRole = roleRepository.findByName(ListRoles.ROLE_SUPER)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: ROLE_SUPER"));
        rolesSet.add(superRole);

        // También puedes agregar más roles si deseas
        // RoleEntity userRole = roleRepository.findByName(ListRoles.ROLE_USER)
        //         .orElseThrow(() -> new RuntimeException("Rol no encontrado: ROLE_USER"));
        // rolesSet.add(userRole);

        return rolesSet;
    }

    // Método auxiliar para obtener los permisos desde la base de datos
    private Set<PermissionsEntity> obtenerPermisos(ListPermission... permissions) {
        Set<PermissionsEntity> permissionsSet = new HashSet<>();

        // Buscar cada permiso en la base de datos
        for (ListPermission permissionEnum : permissions) {
            PermissionsEntity permission = permissionRepository.findByName(permissionEnum)
                    .orElseThrow(() -> new RuntimeException("Permiso no encontrado: " + permissionEnum.name()));
            permissionsSet.add(permission);
        }

        return permissionsSet;
    }

    // Método auxiliar para obtener los permisos por rol
    private Set<PermissionsEntity> obtenerPermisosPorRol(ListRoles roleEnum) {
        Set<PermissionsEntity> permissionsSet = new HashSet<>();

        // Dependiendo del rol, asignar los permisos correspondientes
        switch (roleEnum) {
            case ROLE_SUPER:
                permissionsSet.addAll(obtenerPermisos(
                        ListPermission.USER_READ,
                        ListPermission.USER_CREATE,
                        ListPermission.USER_UPDATE,
                        ListPermission.USER_DELETE,
                        ListPermission.POST_READ,
                        ListPermission.POST_CREATE,
                        ListPermission.POST_UPDATE,
                        ListPermission.POST_DELETE,
                        ListPermission.REPORT_VIEW,
                        ListPermission.REPORT_EXPORT,
                        ListPermission.ADMIN_PANEL_ACCESS
                ));
                break;
            case ROLE_ADMIN:
                permissionsSet.addAll(obtenerPermisos(
                        ListPermission.USER_READ,
                        ListPermission.USER_CREATE,
                        ListPermission.USER_UPDATE,
                        ListPermission.USER_DELETE,
                        ListPermission.POST_READ,
                        ListPermission.POST_CREATE,
                        ListPermission.POST_UPDATE,
                        ListPermission.POST_DELETE,
                        ListPermission.REPORT_VIEW
                ));
                break;
            case ROLE_USER:
                permissionsSet.addAll(obtenerPermisos(
                        ListPermission.USER_READ,
                        ListPermission.USER_CREATE,
                        ListPermission.USER_UPDATE,
                        ListPermission.USER_DELETE
               ));
                break;
            case ROLE_MODERATOR:
                permissionsSet.addAll(obtenerPermisos(
                        ListPermission.POST_READ,
                        ListPermission.POST_UPDATE
                ));
                break;
            case ROLE_GUEST:
                permissionsSet.addAll(obtenerPermisos(
                        ListPermission.POST_READ
                ));
                break;
        }

        return permissionsSet;
    }
}