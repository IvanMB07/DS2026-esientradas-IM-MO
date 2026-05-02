package edu.esi.ds.esiusuarios.dto;

public class EmailRequest {
    private String email;
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
