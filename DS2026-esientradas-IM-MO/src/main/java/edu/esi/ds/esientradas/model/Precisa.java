package edu.esi.ds.esientradas.model;

import jakarta.persistence.Entity;

@Entity
/**
 * nombre_clase: Precisa
 * parametros_clave: fila, columna, planta
 * funcion: subtipo de entrada con asiento exacto
 * flujo_en_el_que_participa: seleccion detallada de asiento
 * comunicacion: Entrada, BusquedaService
 */
public class Precisa extends Entrada {
    private int fila, columna, planta;

    public Precisa() {
        super();
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public int getColumna() {
        return columna;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    public int getPlanta() {
        return planta;
    }

    public void setPlanta(int planta) {
        this.planta = planta;
    }
}
