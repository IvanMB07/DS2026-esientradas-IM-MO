package edu.esi.ds.esientradas.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.esi.ds.esientradas.model.ColaEspera;
import edu.esi.ds.esientradas.model.EstadoCola;

public interface ColaEsperaDao extends JpaRepository<ColaEspera, Long> {

    ColaEspera findFirstByEspectaculoIdAndEmailUsuarioAndEstado(Long espectaculoId, String emailUsuario,
            EstadoCola estado);

    ColaEspera findFirstByEspectaculoIdAndEstadoOrderByHoraSolicitudAscIdAsc(Long espectaculoId, EstadoCola estado);

    ColaEspera findFirstByEspectaculoIdAndEmailUsuarioAndEstadoOrderByHoraSolicitudDescIdDesc(Long espectaculoId,
            String emailUsuario, EstadoCola estado);

    List<ColaEspera> findByEspectaculoIdAndEstadoOrderByHoraSolicitudAscIdAsc(Long espectaculoId, EstadoCola estado);
}
