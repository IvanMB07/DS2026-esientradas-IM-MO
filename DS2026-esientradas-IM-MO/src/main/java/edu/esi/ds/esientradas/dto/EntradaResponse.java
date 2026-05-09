package edu.esi.ds.esientradas.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class EntradaResponse {
    @NotNull
    private Long id;

    @NotNull
    private Long precio;

    @NotNull
    private String estado;

    private Integer zona;
    private Integer fila;
    private Integer columna;
    private Integer planta;

    public EntradaResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPrecio() {
        return precio;
    }

    public void setPrecio(Long precio) {
        this.precio = precio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getZona() {
        return zona;
    }

    public void setZona(Integer zona) {
        this.zona = zona;
    }

    public Integer getFila() {
        return fila;
    }

    public void setFila(Integer fila) {
        this.fila = fila;
    }

    public Integer getColumna() {
        return columna;
    }

    public void setColumna(Integer columna) {
        this.columna = columna;
    }

    public Integer getPlanta() {
        return planta;
    }

    public void setPlanta(Integer planta) {
        this.planta = planta;
    }
}
