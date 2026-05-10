package edu.esi.ds.esiusuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * nombre_clase: AuthRequest
 * funcion: objeto de transferencia para autenticación
 * flujo_en_el_que_participa: logout y validación de token
 * comunicacion: controladores y servicios
 */
public class AuthRequest {
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
    @NotBlank
    @Size(max = 128)
    private String token;

    // Getters y Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}