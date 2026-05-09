package edu.esi.ds.esientradas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SeleccionarRequest {

    @NotNull(message = "El identificador de entrada es obligatorio")
    private Long idEntrada;

    @Size(max = 128, message = "El token de compra no puede exceder 128 caracteres")
    private String compraToken;

    @Size(max = 128, message = "El token de usuario no puede exceder 128 caracteres")
    private String userToken;

    public Long getIdEntrada() {
        return idEntrada;
    }

    public void setIdEntrada(Long idEntrada) {
        this.idEntrada = idEntrada;
    }

    public String getCompraToken() {
        return compraToken;
    }

    public void setCompraToken(String compraToken) {
        this.compraToken = compraToken;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}
