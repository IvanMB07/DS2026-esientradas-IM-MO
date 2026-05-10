package edu.esi.ds.esiusuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * nombre_clase: EmailRequest
 * funcion: objeto de transferencia para envío de email con PDF
 * flujo_en_el_que_participa: envío de correos
 * comunicacion: controladores y servicios
 */
public class EmailRequest {
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 10485760) // 10MB máximo para PDF base64
    private String pdfBase64; // El PDF viajará como texto codificado
    // Getters y Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPdfBase64() {
        return pdfBase64;
    }

    public void setPdfBase64(String pdfBase64) {
        this.pdfBase64 = pdfBase64;
    }
}
