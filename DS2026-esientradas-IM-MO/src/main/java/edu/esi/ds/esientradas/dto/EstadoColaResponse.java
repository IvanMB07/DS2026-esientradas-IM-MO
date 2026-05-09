package edu.esi.ds.esientradas.dto;

import jakarta.validation.constraints.NotNull;

public class EstadoColaResponse {
    @NotNull
    private Long espectaculoId;

    @NotNull
    private Boolean enCola;

    @NotNull
    private Integer posicion;

    private String compraTokenAsignado;

    public EstadoColaResponse() {
    }

    public EstadoColaResponse(Long espectaculoId, Boolean enCola, Integer posicion, String compraTokenAsignado) {
        this.espectaculoId = espectaculoId;
        this.enCola = enCola;
        this.posicion = posicion;
        this.compraTokenAsignado = compraTokenAsignado;
    }

    public Long getEspectaculoId() {
        return espectaculoId;
    }

    public void setEspectaculoId(Long espectaculoId) {
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

    public String getCompraTokenAsignado() {
        return compraTokenAsignado;
    }

    public void setCompraTokenAsignado(String compraTokenAsignado) {
        this.compraTokenAsignado = compraTokenAsignado;
    }
}
