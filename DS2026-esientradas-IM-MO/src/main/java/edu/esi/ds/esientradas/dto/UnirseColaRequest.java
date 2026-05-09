package edu.esi.ds.esientradas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UnirseColaRequest {

    @NotNull(message = "El identificador del espectáculo es obligatorio")
    private Long espectaculoId;

    @Size(max = 128, message = "El token de compra no puede exceder 128 caracteres")
    private String compraToken;

    @Size(max = 128, message = "El token de usuario no puede exceder 128 caracteres")
    private String userToken;

    public Long getEspectaculoId() {
        return espectaculoId;
    }

    public void setEspectaculoId(Long espectaculoId) {
        this.espectaculoId = espectaculoId;
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
