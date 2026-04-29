package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Token;
import jakarta.transaction.Transactional;

@Service
public class PagosService {
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private EntradaDao entradaDao;

    public String prepararPago(Long totalCentimos) throws StripeException {
        if (totalCentimos == null || totalCentimos <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El monto debe ser mayor que 0");
        }

        try {
            if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Clave de Stripe no configurada");
            }

            Stripe.apiKey = stripeSecretKey;
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setCurrency("eur")
                    .setAmount(totalCentimos)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();
        } catch (StripeException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al preparar el pago: " + e.getMessage());
        }
    }

    @Transactional
    public void confirmarPago(String tokenValor) {
        Token token = this.tokenDao.findById(tokenValor).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no encontrado"));

        if (token.getEntradas() == null || token.getEntradas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No hay entradas asociadas al token");
        }

        token.getEntradas().forEach(entrada -> this.entradaDao.updateEstado(entrada.getId(), Estado.VENDIDA));
    }
}
