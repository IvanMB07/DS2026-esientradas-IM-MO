package edu.esi.ds.esiusuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * nombre_clase: AdminAuthRequest
 * funcion: objeto de transferencia para autenticación de administrador
 * flujo_en_el_que_participa: operaciones administrativas
 * comunicacion: controladores y servicios
 */
public class AdminAuthRequest {
    @NotBlank
    @Email
    @Size(max = 100)
    private String adminEmail;
    @NotBlank
    @Size(max = 128)
    private String adminToken;

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminToken() {
        return adminToken;
    }

    public void setAdminToken(String adminToken) {
        this.adminToken = adminToken;
    }
}