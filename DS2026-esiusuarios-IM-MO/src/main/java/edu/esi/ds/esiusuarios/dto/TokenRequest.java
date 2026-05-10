package edu.esi.ds.esiusuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * nombre_clase: TokenRequest
 * funcion: objeto de transferencia para validación de token
 * flujo_en_el_que_participa: validación de sesión
 * comunicacion: controladores y servicios
 */
public class TokenRequest {
    @NotBlank
    @Size(max = 128)
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
