package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dao.EscenarioDao;
import edu.esi.ds.esientradas.model.Escenario;

@Service
public class EscenarioService {

    @Autowired
    private EscenarioDao dao;

    /**
     * nombre_metodo: insertar
     * parametros: escenario
     * funcion: persiste un escenario nuevo y convierte errores tecnicos en
     * respuestas controladas
     * flujo_en_el_que_participa: configuracion de recintos para espectaculos
     * comunicacion: EscenarioDao.save
     */
    public void insertar(Escenario escenario) {
        try {
            this.dao.save(escenario);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No sabemos qué ha pasado, " +
                    "pero hubo error al insertar el escenario" + e.getMessage(), e);
        }
    }

    /**
     * nombre_metodo: eliminar
     * parametros: id
     * funcion: comprueba existencia y elimina un escenario respetando restricciones
     * de integridad
     * flujo_en_el_que_participa: limpieza/mantenimiento de recintos en
     * administracion
     * comunicacion: EscenarioDao.existsById, EscenarioDao.deleteById
     */
    public void eliminar(Long id) {
        try {
            if (!this.dao.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El escenario no existe");
            }
            this.dao.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede eliminar el escenario porque tiene espectáculos asociados", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al eliminar el escenario: " + e.getMessage(), e);
        }
    }
}