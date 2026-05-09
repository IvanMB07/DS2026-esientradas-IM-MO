package edu.esi.ds.esientradas.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import edu.esi.ds.esientradas.model.ColaEspera;
import edu.esi.ds.esientradas.model.EstadoCola;

/**
 * nombre_clase: ColaEsperaDao
 * parametros_clave: ColaEspera, Long
 * funcion: gestión de la persistencia de usuarios en cola para compra de
 * entradas
 * flujo_en_el_que_participa: proceso de compra, control de aforo y gestión de
 * turnos
 * comunicacion: ColaEsperaService, CompraService
 */
public interface ColaEsperaDao extends JpaRepository<ColaEspera, Long> {

        /**
         * nombre_metodo: findFirstByEspectaculoIdAndEmailUsuarioAndEstado
         * parametros: espectaculoId, emailUsuario, estado
         * funcion: busca la entrada más reciente de un usuario en una cola específica
         * flujo_en_el_que_participa: validación de estado de usuario en el proceso
         */
        ColaEspera findFirstByEspectaculoIdAndEmailUsuarioAndEstado(Long espectaculoId, String emailUsuario,
                        EstadoCola estado);

        /**
         * nombre_metodo: findFirstByEspectaculoIdAndEstadoOrderByHoraSolicitudAscIdAsc
         * parametros: espectaculoId, estado
         * funcion: recupera el primer usuario en la cola (el siguiente en ser atendido)
         * flujo_en_el_que_participa: asignación de turnos de compra (FIFO)
         */
        ColaEspera findFirstByEspectaculoIdAndEstadoOrderByHoraSolicitudAscIdAsc(Long espectaculoId, EstadoCola estado);

        /**
         * nombre_metodo:
         * findFirstByEspectaculoIdAndEmailUsuarioAndEstadoOrderByHoraSolicitudDescIdDesc
         * parametros: espectaculoId, emailUsuario, estado
         * funcion: obtiene el último registro de solicitud para un usuario y
         * espectáculo
         * flujo_en_el_que_participa: comprobación de reintentos o última actividad
         */
        ColaEspera findFirstByEspectaculoIdAndEmailUsuarioAndEstadoOrderByHoraSolicitudDescIdDesc(Long espectaculoId,
                        String emailUsuario, EstadoCola estado);

        /**
         * nombre_metodo: findByEspectaculoIdAndEstadoOrderByHoraSolicitudAscIdAsc
         * parametros: espectaculoId, estado
         * funcion: devuelve la lista completa de usuarios en cola ordenados por turno
         * flujo_en_el_que_participa: monitorización y vaciado de colas
         */
        List<ColaEspera> findByEspectaculoIdAndEstadoOrderByHoraSolicitudAscIdAsc(Long espectaculoId,
                        EstadoCola estado);
}