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
        if (tokenReserva == null || tokenReserva.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de reserva no válido");
        }
        if (tokenUsuario == null || tokenUsuario.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de usuario no válido");
        }

        // 1. Obtener email autenticado desde el backend de usuarios
        String emailActual = usuariosService.checkToken(tokenUsuario);
        if (emailActual == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de usuario expirado o inválido");
        }

        // 2. Recuperar la reserva y validar propiedad antes de tocar la BD
        Token token = tokenDao.findById(tokenReserva).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token de reserva no válido"));

        if (token.getEmailUsuario() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La reserva no está vinculada a un usuario autenticado");
        }
        if (!emailActual.equals(token.getEmailUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permiso para confirmar una compra de otro usuario");
        }

        if (token.getEntradas() == null || token.getEntradas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La reserva no contiene entradas");
        }

        // 3. Lógica de BD (Entradas a VENDIDA)
        token.getEntradas().forEach(e -> e.setEstado(Estado.VENDIDA));

        // 4. Generar PDF (PdfService local de esientradas)
        byte[] pdf = pdfService.generarFactura(emailActual, token.getEntradas());

        // 5. Guardar registro (PdfDao local)
        PdfEntidad reg = new PdfEntidad();
        reg.setEmailUsuario(emailActual);
        reg.setContenido(pdf);
        pdfDao.save(reg);

        // 6. ENVIAR AL OTRO BACKEND PARA EL EMAIL
        usuariosService.enviarPdfAExterno(emailActual, pdf);
    }
}