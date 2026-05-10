package edu.esi.ds.esiusuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * nombre_clase: AdminRequest
 * funcion: objeto de transferencia para solicitudes administrativas
 * flujo_en_el_que_participa: gestión de roles de usuario
 * comunicacion: controladores y servicios
 */
public class AdminRequest {
    @NotBlank
    @Email
    @Size(max = 100)
    private String adminEmail;
    @NotBlank
    @Size(max = 128)
    private String adminToken;
    @NotBlank
    @Email
    @Size(max = 100)
    private String targetEmail; // Opcional
    @NotBlank
    @Pattern(regexp = "USER|ADMIN", message = "El rol debe ser USER o ADMIN")
    @Size(max = 20)
    private String newRole; // Opcional

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

    public String getTargetEmail() {
        return targetEmail;
    }

    public void setTargetEmail(String targetEmail) {
        this.targetEmail = targetEmail;
    }

    public String getNewRole() {
        return newRole;
    }

    public void setNewRole(String newRole) {
        this.newRole = newRole;
    }
}