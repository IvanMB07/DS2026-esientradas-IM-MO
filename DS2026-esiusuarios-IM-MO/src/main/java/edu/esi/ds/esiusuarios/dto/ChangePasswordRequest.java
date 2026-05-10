package edu.esi.ds.esiusuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * nombre_clase: ChangePasswordRequest
 * funcion: objeto de transferencia para cambio de contraseña
 * flujo_en_el_que_participa: gestión de contraseña
 * comunicacion: controladores y servicios
 */
public class ChangePasswordRequest {
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
    @NotBlank
    @Size(max = 128)
    private String token;
    @NotBlank
    @Size(min = 8, max = 128)
    private String currentPwd;
    @NotBlank
    @Size(min = 8, max = 128)
    private String newPwd;

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

    public String getCurrentPwd() {
        return currentPwd;
    }

    public void setCurrentPwd(String currentPwd) {
        this.currentPwd = currentPwd;
    }

    public String getNewPwd() {
        return newPwd;
    }

    public void setNewPwd(String newPwd) {
        this.newPwd = newPwd;
    }
}