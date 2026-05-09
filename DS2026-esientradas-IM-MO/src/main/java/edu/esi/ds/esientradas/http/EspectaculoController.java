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

import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.services.EspectaculoService;
import edu.esi.ds.esientradas.services.UsuariosService;

@RestController
@RequestMapping("/espectaculos")
public class EspectaculoController {

    @Autowired
    private EspectaculoService service;

    @Autowired
    private UsuariosService usuariosService;

    @PostMapping("/insertar")
    public void insertar(@Valid @RequestBody Espectaculo espectaculo, @RequestParam String userToken) {
        // Validar token y que sea ADMIN
        if (!usuariosService.isAdmin(userToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un administrador puede insertar espectáculos");
        }

        if (espectaculo.getArtista() == null || espectaculo.getArtista().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del artista no puede ser nulo o vacío");
        }
        if (espectaculo.getFecha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha del espectáculo no puede ser nula");
        }
        if (espectaculo.getEscenario() == null || espectaculo.getEscenario().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El escenario no puede ser nulo");
        }

        this.service.insertar(espectaculo);
    }

    @DeleteMapping("/eliminar/{id}")
    public void eliminar(@PathVariable Long id, @RequestParam String userToken) {
        // Validar token y que sea ADMIN
        if (!usuariosService.isAdmin(userToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un administrador puede eliminar espectáculos");
        }

        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El ID del espectáculo no es válido");
        }

        this.service.eliminar(id);
    }
}
