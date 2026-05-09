
package edu.esi.ds.esientradas.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.esi.ds.esientradas.model.Espectaculo;

/**
 * nombre_clase: EspectaculoDao
 * parametros_clave: Espectaculo, Long
 * funcion: acceso CRUD y consultas de catalogo de espectaculos
 * flujo_en_el_que_participa: alta/baja admin y busqueda en frontend
 * comunicacion: EspectaculoService, BusquedaService
 */
public interface EspectaculoDao extends JpaRepository<Espectaculo, Long> {
     /**
      * nombre_metodo: findByArtista
      * parametros: artista
      * funcion: devuelve espectaculos filtrados por artista
      * flujo_en_el_que_participa: busqueda textual de catalogo
      */
     List<Espectaculo> findByArtista(String artista);

     /**
      * nombre_metodo: findByEscenarioId
      * parametros: idEscenario
      * funcion: devuelve espectaculos de un escenario concreto
      * flujo_en_el_que_participa: navegacion por recinto
      */
     List<Espectaculo> findByEscenarioId(Long idEscenario);

}