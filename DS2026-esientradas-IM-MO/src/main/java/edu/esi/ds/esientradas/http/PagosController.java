package edu.esi.ds.esientradas.http;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/pagos")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PagosController {

    @Autowired
    private PagosService pagosService;

    @Value("${stripe.publishable.key:}")
    private String stripePublishableKey;

    @PostMapping("/prepararPago")
    public String prepararPago(@RequestBody Map<String, Object> infoPago) throws StripeException {
        Long centimos = null;

        if (infoPago.get("centimos") != null) {
            centimos = ((Number) infoPago.get("centimos")).longValue();
        } else if (infoPago.get("totalCentimos") != null) {
            centimos = ((Number) infoPago.get("totalCentimos")).longValue();
        } else if (infoPago.get("monto") != null) {
            centimos = ((Number) infoPago.get("monto")).longValue();
        }

        if (centimos == null || centimos <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Se requiere centimos, totalCentimos o monto en el request (mayor que 0)");
        }

        try {
            return pagosService.prepararPago(centimos);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error de Stripe: " + e.getMessage(), e);
        }
    }

    // Paso 2: El frontend confirma que Stripe procesó el pago
    @PostMapping("/confirmar")
    public ResponseEntity<String> confirmar(@RequestParam String tokenPrerreserva, @RequestParam String tokenUsuario) {
        pagosService.confirmarPago(tokenPrerreserva, tokenUsuario);
        return ResponseEntity.ok("Pago confirmado y entradas enviadas");
    }

    @GetMapping("/publicKey")
    public ResponseEntity<String> getPublishableKey() {
        if (this.stripePublishableKey == null || this.stripePublishableKey.isEmpty()) {
            return ResponseEntity.status(500).body("");
        }
        return ResponseEntity.ok(this.stripePublishableKey);
    }
}
