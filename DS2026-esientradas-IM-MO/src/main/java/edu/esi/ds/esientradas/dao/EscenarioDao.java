package edu.esi.ds.esientradas.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.esi.ds.esientradas.model.Escenario;

/**
 * nombre_clase: EscenarioDao
 * parametros_clave: Escenario, Long
 * funcion: acceso CRUD a escenarios
 * flujo_en_el_que_participa: gestion de catalogo y busqueda de recintos
 * comunicacion: EscenarioService, BusquedaService
 */
public interface EscenarioDao extends JpaRepository<Escenario, Long> {

}
