package edu.esi.ds.esientradas.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

    @Value("${app.security.admin-email:}")
    private String adminEmail;

    @PostMapping("/insertar")
    public void insertar(@RequestBody Escenario escenario, @RequestParam String userToken) {
        if (adminEmail == null || adminEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No hay un administrador configurado para insertar escenarios");
        }

        String emailActual = usuariosService.checkToken(userToken);
        if (emailActual == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de usuario no válido");
        }
        if (!adminEmail.equalsIgnoreCase(emailActual)) {
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
}
