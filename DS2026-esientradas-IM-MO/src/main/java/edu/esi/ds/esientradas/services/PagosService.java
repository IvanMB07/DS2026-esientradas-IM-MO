package edu.esi.ds.esientradas.services;

import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.model.Entrada;
import jakarta.transaction.Transactional;

@Service
public class PagosService {
    @Value("${stripe.secret.key}") // La pondremos en application.properties
    private String stripeSecretKey;

    // Aquí inyectarías tus servicios de PDF y Email cuando los crees
    // @Autowired private PdfService pdfService;

    public String prepararPago(Long totalCentimos) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setCurrency("eur")
                .setAmount(totalCentimos)
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        JSONObject jso = new JSONObject(intent.toJson());
        String clientSecret = jso.getString("client_secret");
        System.out.println("Client Secret: " + clientSecret);
        return intent.getClientSecret();
    }

     @Transactional
    public void confirmarPago(String tokenPrerreserva) {
        // 1. Buscar entradas asociadas al token de prerreserva
        // 2. Cambiar su estado a VENDIDA
        // 3. Generar PDF y enviar por email (Pasos finales)
    }
}
