package edu.esi.ds.esiusuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * nombre_clase: LoginRequest
 * funcion: objeto de transferencia para login
 * flujo_en_el_que_participa: autenticación
 * comunicacion: controladores y servicios
 */
public class LoginRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String pwd;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}