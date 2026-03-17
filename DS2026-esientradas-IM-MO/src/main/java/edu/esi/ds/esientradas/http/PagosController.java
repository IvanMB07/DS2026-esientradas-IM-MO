package edu.esi.ds.esientradas.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.exception.StripeException;

import edu.esi.ds.esientradas.services.PagosService;

@RestController
@RequestMapping("/pagos")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PagosController {

    @Autowired
    private PagosService pagosService;

    @PostMapping("/prepararPago")
    public String prepararPago(@RequestBody Map<String, Object> infoPago) throws StripeException {
        Long centimos = ((Number) infoPago.get("totalCentimos")).longValue();
        try {
            return pagosService.prepararPago(centimos);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear el intento de pago", e);
        }
    }

    // Paso 2: El frontend confirma que Stripe procesó el pago
    @PostMapping("/confirmar")
    public ResponseEntity<String> confirmar(@RequestParam String tokenPrerreserva) {
        pagosService.confirmarPago(tokenPrerreserva);
        return ResponseEntity.ok("Pago confirmado y entradas enviadas");
    }
}
