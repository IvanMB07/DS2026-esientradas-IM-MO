package edu.esi.ds.esiusuarios.dto;

import java.util.List;
import java.util.Map;

/**
 * ProcesarCompraRequest - DTO para recibir datos de compra desde esientradas
 */
public class ProcesarCompraRequest {
    private String email;
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
