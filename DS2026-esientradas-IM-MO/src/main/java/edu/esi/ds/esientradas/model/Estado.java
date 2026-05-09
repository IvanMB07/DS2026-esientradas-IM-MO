package edu.esi.ds.esientradas.model;

/**
 * nombre_clase: Estado
 * parametros_clave: DISPONIBLE, RESERVADA, VENDIDA
 * funcion: ciclo de vida funcional de una entrada
 * flujo_en_el_que_participa: busqueda/reserva/pago
 * comunicacion: Entrada, EntradaDao, ReservasService, PagosService
 */
public enum Estado {
    DISPONIBLE, RESERVADA, VENDIDA
}
