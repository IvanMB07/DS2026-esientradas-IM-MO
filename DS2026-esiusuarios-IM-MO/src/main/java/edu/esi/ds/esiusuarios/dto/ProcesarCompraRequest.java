package edu.esi.ds.esiusuarios.dto;

import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ProcesarCompraRequest - DTO para recibir datos de compra desde esientradas
 */
public class ProcesarCompraRequest {
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
    @NotNull
    @NotEmpty
    private List<Map<String, String>> entradas;

    public ProcesarCompraRequest() {
    }

    public ProcesarCompraRequest(String email, List<Map<String, String>> entradas) {
        this.email = email;
        this.entradas = entradas;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Map<String, String>> getEntradas() {
        return entradas;
    }

    public void setEntradas(List<Map<String, String>> entradas) {
        this.entradas = entradas;
    }
}
