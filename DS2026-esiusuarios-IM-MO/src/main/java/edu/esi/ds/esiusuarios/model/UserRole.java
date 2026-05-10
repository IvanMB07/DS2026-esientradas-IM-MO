package edu.esi.ds.esiusuarios.model;

/**
 * nombre_clase: UserRole
 * funcion: enumeración de roles de usuario en el sistema
 * flujo_en_el_que_participa: autorización y gestión de permisos
 * comunicacion: User, UserService
 */
/**
 * Enum de roles de usuario
 * - USER: Cliente normal, puede acceder a sus datos
 * - ADMIN: Administrador, puede eliminar usuarios y cambiar roles
 */
public enum UserRole {
    USER,
    ADMIN
}
