package edu.esi.ds.esientradas.http;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import edu.esi.ds.esientradas.model.Token;
import edu.esi.ds.esientradas.services.ReservasService;
import edu.esi.ds.esientradas.services.UsuariosService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/reservas")
public class ReservasController {

    @Autowired
    private ReservasService service;
    @Autowired
    private UsuariosService usuariosService;

    @PostMapping("/seleccionar")
    public String seleccionar(@RequestBody Map<String, Object> payload) {
        Long idEntrada = Long.parseLong(payload.get("idEntrada").toString());
        String cToken = payload.get("compraToken") != null ? payload.get("compraToken").toString() : null;
        String uToken = payload.get("userToken") != null ? payload.get("userToken").toString() : null;

        return this.service.seleccionarEntrada(idEntrada, cToken, uToken);
    }

    @PostMapping("/cancelar")
    public void cancelar(@RequestBody Map<String, Object> payload) {
        Long idEntrada = Long.parseLong(payload.get("idEntrada").toString());
        String cToken = payload.get("compraToken").toString();
        String uToken = payload.get("userToken") != null ? payload.get("userToken").toString() : null;

        this.service.cancelarEntrada(idEntrada, cToken, uToken);
    }

    @GetMapping("/resumen")
    public Object getResumen(@RequestParam String compraToken,
            @RequestParam(required = false) String userToken) {
        // IMPORTANTE: No valides aquí el token, deja que el Service lo haga
        // para que la lógica de "Usuario Anónimo" (Escenario B) funcione.
        return this.service.getResumenCompra(compraToken, userToken);
    }

    @GetMapping("/carritos-usuario")
    public List<Token> getCarritosUsuario(@RequestParam String userToken) {
        return this.service.getCarritosDelUsuario(userToken);
    }
}