package edu.esi.ds.esientradas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public class PreparePaymentRequest {

    @Positive(message = "El importe debe ser un valor positivo")
    private Long centimos;

    @Positive(message = "El importe debe ser un valor positivo")
    private Long totalCentimos;

    @Positive(message = "El importe debe ser un valor positivo")
    private Long monto;

    public Long getCentimos() {
        return centimos;
    }

    public void setCentimos(Long centimos) {
        this.centimos = centimos;
    }

    public Long getTotalCentimos() {
        return totalCentimos;
    }

    public void setTotalCentimos(Long totalCentimos) {
        this.totalCentimos = totalCentimos;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }
}
