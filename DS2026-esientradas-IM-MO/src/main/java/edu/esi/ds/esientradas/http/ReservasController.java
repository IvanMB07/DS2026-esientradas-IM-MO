package edu.esi.ds.esientradas.http;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import edu.esi.ds.esientradas.dto.CancelarRequest;
import edu.esi.ds.esientradas.dto.SeleccionarRequest;
import edu.esi.ds.esientradas.dto.UnirseColaRequest;
import edu.esi.ds.esientradas.dto.ColaResponse;
import edu.esi.ds.esientradas.dto.EstadoColaResponse;

import edu.esi.ds.esientradas.model.Token;
import edu.esi.ds.esientradas.services.ReservasService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/reservas")
public class ReservasController {

    @Autowired
    private ReservasService service;

    @PostMapping("/seleccionar")
    public String seleccionar(@Valid @RequestBody SeleccionarRequest payload) {
        Long idEntrada = payload.getIdEntrada();
        String cToken = payload.getCompraToken();
        String uToken = payload.getUserToken();

        return this.service.seleccionarEntrada(idEntrada, cToken, uToken);
    }

    @PostMapping("/cancelar")
    public void cancelar(@Valid @RequestBody CancelarRequest payload) {
        Long idEntrada = payload.getIdEntrada();
        String cToken = payload.getCompraToken();
        String uToken = payload.getUserToken();

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

    @PostMapping("/cola/unirse")
    public ColaResponse unirseCola(@Valid @RequestBody UnirseColaRequest payload) {
        Long espectaculoId = payload.getEspectaculoId();
        String cToken = payload.getCompraToken();
        String uToken = payload.getUserToken();

        java.util.Map<String, Object> result = this.service.unirseCola(espectaculoId, cToken, uToken);
        return new ColaResponse(
                (Boolean) result.get("enCola"),
                (Integer) result.get("posicion"),
                (Long) result.get("espectaculoId"));
    }

    @GetMapping("/cola/estado")
    public EstadoColaResponse estadoCola(@RequestParam Long espectaculoId,
            @RequestParam String userToken) {
        java.util.Map<String, Object> result = this.service.estadoCola(espectaculoId, userToken);
        return new EstadoColaResponse(
                espectaculoId,
                (Boolean) result.get("enCola"),
                (Integer) result.get("posicion"),
                (String) result.get("compraTokenAsignado"));
    }
}
