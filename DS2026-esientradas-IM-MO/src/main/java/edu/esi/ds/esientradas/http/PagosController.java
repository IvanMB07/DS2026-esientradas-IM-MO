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
// Clave: CORS restringido al origen del frontend local de la practica.
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PagosController {

    @Autowired
    private PagosService pagosService;

    @Value("${stripe.publishable.key:}")
    private String stripePublishableKey;

    /**
     * nombre_metodo: prepararPago
     * parametros: infoPago (contiene centimos, totalCentimos o monto)
     * funcion: normaliza el importe, valida formato y crea el PaymentIntent en
     * Stripe
     * flujo_en_el_que_participa: inicio del flujo de compra antes de confirmar pago
     */
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

    /**
     * nombre_metodo: confirmar
     * parametros: tokenPrerreserva, tokenUsuario
     * funcion: confirma la compra en backend una vez Stripe ha validado el cobro
     * flujo_en_el_que_participa: cierre del flujo de pago y emision de
     * entradas/factura
     */
    @PostMapping("/confirmar")
    public ResponseEntity<String> confirmar(@RequestParam String tokenPrerreserva, @RequestParam String tokenUsuario) {
        pagosService.confirmarPago(tokenPrerreserva, tokenUsuario);
        return ResponseEntity.ok("Pago confirmado y entradas enviadas");
    }

    /**
     * nombre_metodo: getPublishableKey
     * parametros: ninguno
     * funcion: expone la clave publica de Stripe para inicializar el pago en
     * frontend
     * flujo_en_el_que_participa: preparacion del formulario de pago en cliente
     */
    @GetMapping("/publicKey")
    public ResponseEntity<String> getPublishableKey() {
        if (this.stripePublishableKey == null || this.stripePublishableKey.isEmpty()) {
            return ResponseEntity.status(500).body("");
        }
        return ResponseEntity.ok(this.stripePublishableKey);
    }
}
