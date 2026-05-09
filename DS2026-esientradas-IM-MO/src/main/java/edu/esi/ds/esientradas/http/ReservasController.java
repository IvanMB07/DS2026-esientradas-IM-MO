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
// Clave: CORS limitado al frontend Angular local para evitar origenes no
// esperados.
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/reservas")
public class ReservasController {

    @Autowired
    private ReservasService service;

    /**
     * nombre_metodo: seleccionar
     * parametros: payload (idEntrada, compraToken, userToken)
     * funcion: intenta reservar una entrada concreta dentro de un carrito activo
     * flujo_en_el_que_participa: fase de seleccion de entradas durante la compra
     */
    @PostMapping("/seleccionar")
    public String seleccionar(@Valid @RequestBody SeleccionarRequest payload) {
        Long idEntrada = payload.getIdEntrada();
        String cToken = payload.getCompraToken();
        String uToken = payload.getUserToken();

        return this.service.seleccionarEntrada(idEntrada, cToken, uToken);
    }

    /**
     * nombre_metodo: cancelar
     * parametros: payload (idEntrada, compraToken, userToken)
     * funcion: libera una entrada previamente seleccionada por el usuario
     * flujo_en_el_que_participa: gestion de carrito antes del pago
     */
    @PostMapping("/cancelar")
    public void cancelar(@Valid @RequestBody CancelarRequest payload) {
        Long idEntrada = payload.getIdEntrada();
        String cToken = payload.getCompraToken();
        String uToken = payload.getUserToken();

        this.service.cancelarEntrada(idEntrada, cToken, uToken);
    }

    /**
     * nombre_metodo: getResumen
     * parametros: compraToken, userToken (opcional)
     * funcion: devuelve el estado resumido del carrito asociado al token de compra
     * flujo_en_el_que_participa: consulta del estado de compra previa al pago
     */
    @GetMapping("/resumen")
    public Object getResumen(@RequestParam String compraToken,
            @RequestParam(required = false) String userToken) {
        // IMPORTANTE: No valides aquí el token, deja que el Service lo haga
        // para que la lógica de "Usuario Anónimo" (Escenario B) funcione.
        return this.service.getResumenCompra(compraToken, userToken);
    }

    /**
     * nombre_metodo: getCarritosUsuario
     * parametros: userToken
     * funcion: recupera los carritos activos del usuario autenticado
     * flujo_en_el_que_participa: recuperacion de carritos al reanudar una sesion
     */
    @GetMapping("/carritos-usuario")
    public List<Token> getCarritosUsuario(@RequestParam String userToken) {
        return this.service.getCarritosDelUsuario(userToken);
    }

    /**
     * nombre_metodo: unirseCola
     * parametros: payload (espectaculoId, compraToken, userToken)
     * funcion: registra al usuario en la cola de espera de un espectaculo sin
     * plazas
     * flujo_en_el_que_participa: gestion de saturacion cuando no hay entradas
     * disponibles
     */
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

    /**
     * nombre_metodo: estadoCola
     * parametros: espectaculoId, userToken
     * funcion: consulta posicion y estado actual del usuario en la cola
     * flujo_en_el_que_participa: seguimiento de cola hasta asignacion de token de
     * compra
     */
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
