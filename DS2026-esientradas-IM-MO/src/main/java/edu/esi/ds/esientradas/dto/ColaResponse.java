package edu.esi.ds.esientradas.dto;

import jakarta.validation.constraints.NotNull;

public class ColaResponse {
    @NotNull
    private Boolean enCola;

    @NotNull
    private Integer posicion;

    @NotNull
    private Long espectaculoId;

    public ColaResponse() {
    }

    public ColaResponse(Boolean enCola, Integer posicion, Long espectaculoId) {
        this.enCola = enCola;
        this.posicion = posicion;
        this.espectaculoId = espectaculoId;
    }

    public Boolean getEnCola() {
        return enCola;
    }

    public void setEnCola(Boolean enCola) {
        this.enCola = enCola;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public Long getEspectaculoId() {
        return espectaculoId;
    }

    public void setEspectaculoId(Long espectaculoId) {
        this.espectaculoId = espectaculoId;
    }
}
