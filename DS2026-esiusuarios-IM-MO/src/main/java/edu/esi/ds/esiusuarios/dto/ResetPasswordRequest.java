package edu.esi.ds.esiusuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * nombre_clase: ResetPasswordRequest
 * funcion: objeto de transferencia para reset de contraseña
 * flujo_en_el_que_participa: recuperación de contraseña
 * comunicacion: controladores y servicios
 */
public class ResetPasswordRequest {
    @NotBlank
    @Size(max = 128)
    private String token;
    @NotBlank
    @Size(min = 8, max = 128)
    private String pwd;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}