package edu.esi.ds.esientradas.http;

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
import jakarta.validation.Valid;

import edu.esi.ds.esientradas.dto.PreparePaymentRequest;
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
    public String prepararPago(@Valid @RequestBody PreparePaymentRequest infoPago) throws StripeException {
        Long centimos = null;

        if (infoPago.getCentimos() != null) {
            centimos = infoPago.getCentimos();
        } else if (infoPago.getTotalCentimos() != null) {
            centimos = infoPago.getTotalCentimos();
        } else if (infoPago.getMonto() != null) {
            centimos = infoPago.getMonto();
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
