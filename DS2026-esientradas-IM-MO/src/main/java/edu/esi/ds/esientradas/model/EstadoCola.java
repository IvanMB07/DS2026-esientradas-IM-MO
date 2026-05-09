package edu.esi.ds.esientradas.model;

/**
 * nombre_clase: EstadoCola
 * parametros_clave: EN_COLA, ATENDIDA, CANCELADA
 * funcion: estado funcional de un usuario en la cola de espera
 * flujo_en_el_que_participa: gestion de espera y asignacion diferida de
 * entradas
 * comunicacion: ColaEspera, ColaEsperaService
 */
public enum EstadoCola {
    EN_COLA,
    ATENDIDA,
    CANCELADA
}
