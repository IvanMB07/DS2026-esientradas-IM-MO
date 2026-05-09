package edu.esi.ds.esientradas.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;

/**
 * nombre_clase: EntradaDao
 * parametros_clave: Entrada, Long
 * funcion: consultas y actualizaciones sobre entradas y su estado
 * flujo_en_el_que_participa: busqueda, reserva, pago, liberacion y cola
 * comunicacion: BusquedaService, ReservasService, PagosService, TokenService,
 * ColaEsperaService
 */
public interface EntradaDao extends JpaRepository<Entrada, Long> {
     /**
      * nombre_metodo: findByEspectaculoId
      * parametros: espectaculoId
      * funcion: lista todas las entradas de un espectaculo
      * flujo_en_el_que_participa: catalogo y resumen de disponibilidad
      */
     List<Entrada> findByEspectaculoId(Long espectaculoId);

     /**
      * nombre_metodo: findFirstByEspectaculoIdAndEstadoOrderByIdAsc
      * parametros: espectaculoId, estado
      * funcion: obtiene la primera entrada libre/filtrada por estado
      * flujo_en_el_que_participa: asignacion en cola de espera
      */
     Entrada findFirstByEspectaculoIdAndEstadoOrderByIdAsc(Long espectaculoId, Estado estado);

     /**
      * nombre_metodo: updateEstado
      * parametros: idEntrada, estado
      * funcion: actualiza estado de una entrada por id
      * flujo_en_el_que_participa: cambios de estado en reserva/pago/cancelacion
      */
     @Query(value = "UPDATE Entrada e SET e.estado = :estado WHERE e.id = :idEntrada")
     @Modifying // Esto se pone porque modifica la base de datos, es un update
     void updateEstado(@Param("idEntrada") Long idEntrada, @Param("estado") Estado estado);

     /**
      * nombre_metodo: countByEspectaculoId
      * parametros: espectaculoId
      * funcion: cuenta entradas totales de un espectaculo
      * flujo_en_el_que_participa: metricas de aforo
      */
     Integer countByEspectaculoId(Long espectaculoId);

     /**
      * nombre_metodo: countByEspectaculoIdAndEstado
      * parametros: espectaculoId, estado
      * funcion: cuenta entradas por estado para un espectaculo
      * flujo_en_el_que_participa: disponibilidad en tiempo real
      */
     Integer countByEspectaculoIdAndEstado(Long espectaculoId, Estado estado);

     /**
      * nombre_metodo: getNumeroDeEntradasComoDto
      * parametros: espectaculoId
      * funcion: devuelve agregados (total/libres/reservadas/vendidas) en una
      * consulta nativa
      * flujo_en_el_que_participa: endpoints de resumen estadistico
      */
     @Query(value = """
               SELECT COUNT(*) AS total,
                    SUM(estado = 'DISPONIBLE') AS libres,
                    SUM(estado = 'RESERVADA') AS reservadas,
                    SUM(estado = 'VENDIDA') AS vendidas
               FROM Entrada
               WHERE espectaculo_id = :espectaculoId""", nativeQuery = true)
     Object getNumeroDeEntradasComoDto(@Param("espectaculoId") Long espectaculoId);
}