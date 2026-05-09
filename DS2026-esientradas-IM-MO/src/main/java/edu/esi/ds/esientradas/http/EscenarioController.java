package edu.esi.ds.esientradas.http;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.services.EscenarioService;
import edu.esi.ds.esientradas.services.UsuariosService;

@RestController
@RequestMapping("/escenarios")
public class EscenarioController {

    @Autowired
    private EscenarioService service;

    @Autowired
    private UsuariosService usuariosService;

    /**
     * nombre_metodo: insertar
     * parametros: escenario, userToken
     * funcion: valida rol ADMIN y crea un escenario nuevo
     * flujo_en_el_que_participa: administracion de recintos para espectaculos
     */
    @PostMapping("/insertar")
    public void insertar(@Valid @RequestBody Escenario escenario, @RequestParam String userToken) {
        // Validar token y que sea ADMIN
        if (!usuariosService.isAdmin(userToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un administrador puede insertar escenarios");
        }

        if (escenario.getNombre() == null || escenario.getNombre().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del escenario no puede ser nulo o vacío");
        }
        if (escenario.getDescripcion() == null || escenario.getDescripcion().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La descipción del escenario no puede ser nula o vacía");
        }
        this.service.insertar(escenario);
    }

    /**
     * nombre_metodo: eliminar
     * parametros: id, userToken
     * funcion: valida rol ADMIN y elimina un escenario existente
     * flujo_en_el_que_participa: mantenimiento de recintos en administracion
     */
    @DeleteMapping("/eliminar/{id}")
    public void eliminar(@PathVariable Long id, @RequestParam String userToken) {
        // Validar token y que sea ADMIN
        if (!usuariosService.isAdmin(userToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un administrador puede eliminar escenarios");
        }

        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El ID del escenario no es válido");
        }

        this.service.eliminar(id);
    }
}
