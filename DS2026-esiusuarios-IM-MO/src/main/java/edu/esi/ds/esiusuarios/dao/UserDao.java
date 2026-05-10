package edu.esi.ds.esiusuarios.dao;

import org.springframework.data.repository.CrudRepository;
import edu.esi.ds.esiusuarios.model.User;
import java.util.Optional;

/**
 * nombre_clase: UserDao
 * funcion: interfaz de acceso a datos para usuarios
 * flujo_en_el_que_participa: persistencia de usuarios
 * comunicacion: base de datos SQL Server
 */
public interface UserDao extends CrudRepository<User, String> {
    // Spring es tan listo que con este nombre de método ya sabe buscar por email
    /**
     * nombre_metodo: findByEmail
     * parametros: email
     * funcion: busca usuario por email
     * flujo_en_el_que_participa: autenticación y registro
     */
    Optional<User> findByEmail(String email);

    // Nueva búsqueda para encontrar al usuario por su token de recuperación
    /**
     * nombre_metodo: findByPwdRecoveryToken
     * parametros: token
     * funcion: busca usuario por token de recuperación de contraseña
     * flujo_en_el_que_participa: recuperación de contraseña
     */
    Optional<User> findByPwdRecoveryToken(String token);

    // Nueva búsqueda para encontrar al usuario por su token de autenticación
    /**
     * nombre_metodo: findByToken
     * parametros: token
     * funcion: busca usuario por token de sesión
     * flujo_en_el_que_participa: validación de sesión
     */
    Optional<User> findByToken(String token);
}