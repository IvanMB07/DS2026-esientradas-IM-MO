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
import edu.esi.ds.esientradas.dao.PdfDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.PdfEntidad;
import edu.esi.ds.esientradas.model.Token;
import jakarta.transaction.Transactional;

@Service
public class PagosService {
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private UsuariosService usuariosService;

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private PdfDao pdfDao;

    @Autowired
    private PdfService pdfService;

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
    public void confirmarPago(String tokenReserva, String tokenUsuario) {
        // 1. Obtener email (Comunicación Backend -> Backend)
        String email = usuariosService.checkToken(tokenUsuario);

        // 2. Lógica de BD (Entradas a VENDIDA)
        Token token = tokenDao.findById(tokenReserva).get();
        token.getEntradas().forEach(e -> e.setEstado(Estado.VENDIDA));

        // 3. Generar PDF (PdfService local de esientradas)
        byte[] pdf = pdfService.generarFactura(email, token.getEntradas());

        // 4. Guardar registro (PdfDao local)
        PdfEntidad reg = new PdfEntidad();
        reg.setEmailUsuario(email);
        reg.setContenido(pdf);
        pdfDao.save(reg);

        // 5. ENVIAR AL OTRO BACKEND PARA EL EMAIL
        usuariosService.enviarPdfAExterno(email, pdf);
    }
}