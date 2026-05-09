package edu.esi.ds.esientradas.model;

import jakarta.persistence.Entity;

@Entity
/**
 * nombre_clase: DeZona
 * parametros_clave: zona
 * funcion: subtipo de entrada con localizacion por zona general
 * flujo_en_el_que_participa: catalogo de entradas por zona y reserva asociada
 * comunicacion: Entrada, BusquedaService
 */
public class DeZona extends Entrada {
    private Integer zona;

    public DeZona() {
        super();
    }

    public Integer getZona() {
        return zona;
    }

    public void setZona(Integer zona) {
        this.zona = zona;
    }
}
