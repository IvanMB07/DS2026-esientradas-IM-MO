package edu.esi.ds.esiusuarios.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.time.LocalDateTime; // Necesario para la fecha de caducidad

/**
 * nombre_clase: User
 * funcion: entidad de usuario
 * flujo_en_el_que_participa: persistencia y gestión de usuarios
 * comunicacion: base de datos SQL Server
 */
@Entity
@Table(name = "usuarios") // Nombre de la tabla en SQL Server
public class User {
    @Id
    private String email; // Usaremos el email como llave única (ID)
    private String name;
    private String password; // Aquí guardaremos la contraseña YA ENCRIPTADA
    private String token;
    private String pwdRecoveryToken;
    private LocalDateTime pwdRecoveryTokenExpiry;

    @Enumerated(EnumType.STRING)
    private UserRole role; // USER por defecto, puede cambiar a ADMIN

    // IMPORTANTE: Hibernate necesita un constructor vacío para funcionar
    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = UserRole.USER; // Por defecto, todo usuario nuevo es USER
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPwdRecoveryToken() {
        return pwdRecoveryToken;
    }

    public void setPwdRecoveryToken(String pwdRecoveryToken) {
        this.pwdRecoveryToken = pwdRecoveryToken;
    }

    public LocalDateTime getPwdRecoveryTokenExpiry() {
        return pwdRecoveryTokenExpiry;
    }

    public void setPwdRecoveryTokenExpiry(LocalDateTime pwdRecoveryTokenExpiry) {
        this.pwdRecoveryTokenExpiry = pwdRecoveryTokenExpiry;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

}
