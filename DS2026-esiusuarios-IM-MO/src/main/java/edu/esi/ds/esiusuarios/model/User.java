package edu.esi.ds.esiusuarios.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios") // Nombre de la tabla en SQL Server
public class User {
    @Id
    private String email; // Usaremos el email como llave única (ID)
    private String name;
    private String password; // Aquí guardaremos la contraseña YA ENCRIPTADA
    private String token;

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
}
