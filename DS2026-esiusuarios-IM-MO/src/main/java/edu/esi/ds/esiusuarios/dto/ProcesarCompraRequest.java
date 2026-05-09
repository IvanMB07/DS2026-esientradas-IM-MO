package edu.esi.ds.esiusuarios.dto;

import java.util.List;
import jakarta.validation.Valid;
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
    @Valid
    private List<EntradaData> entradas;

    public ProcesarCompraRequest() {
    }

    public ProcesarCompraRequest(String email, List<EntradaData> entradas) {
        this.email = email;
        this.entradas = entradas;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<EntradaData> getEntradas() {
        return entradas;
    }

    public void setEntradas(List<EntradaData> entradas) {
        this.entradas = entradas;
    }
}
