package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dao.EspectaculoDao;
import edu.esi.ds.esientradas.model.Espectaculo;

@Service
public class EspectaculoService {

    @Autowired
    private EspectaculoDao dao;

    public void insertar(Espectaculo espectaculo) {
        try {
            this.dao.save(espectaculo);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No sabemos qué ha pasado, pero hubo error al insertar el espectáculo: " + e.getMessage(),
                    e);
        }
    }

    public void eliminar(Long id) {
        try {
            if (!this.dao.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El espectáculo no existe");
            }
            this.dao.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede eliminar el espectáculo porque tiene entradas asociadas", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al eliminar el espectáculo: " + e.getMessage(), e);
        }
    }
}
