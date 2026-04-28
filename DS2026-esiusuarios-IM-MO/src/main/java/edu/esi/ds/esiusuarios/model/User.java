package edu.esi.ds.esiusuarios.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime; // Necesario para la fecha de caducidad

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

    // IMPORTANTE: Hibernate necesita un constructor vacío para funcionar
    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
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

}
