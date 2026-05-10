package edu.esi.ds.esiusuarios.http;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esiusuarios.dto.EmailRequest;
import edu.esi.ds.esiusuarios.dto.ProcesarCompraRequest;
import edu.esi.ds.esiusuarios.services.EmailService;
import edu.esi.ds.esiusuarios.services.EmailTemplateService;
import edu.esi.ds.esiusuarios.services.GmailService;
import edu.esi.ds.esiusuarios.services.MediadorService;
import edu.esi.ds.esiusuarios.services.UserService;
import jakarta.mail.MessagingException;

@CrossOrigin(origins = "http://localhost:4200") // Limitado al frontend Angular local.
@RestController
/**
 * nombre_clase: ExternalController
 * funcion: control de endpoints externos para comunicación con otros
 * microservicios
 * flujo_en_el_que_participa: validación de tokens, envío de correos,
 * procesamiento de compras
 * comunicacion: esientradas, frontend Angular
 */
@RequestMapping("/external")
public class ExternalController {

    @Autowired
    private UserService service;

    @Autowired
    private GmailService gmailService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private MediadorService mediadorService;

    // @Autowired
    // private EmailService emailService;

    /**
     * nombre_metodo: checkToken
     * parametros: token
     * funcion: valida token y devuelve email asociado
     * flujo_en_el_que_participa: validación de sesión
     */
    @GetMapping("/checkToken/{token}")
    public String checkToken(@PathVariable String token) {
        // 1. Validamos que el token no venga vacío
        if (token == null || token.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }

        // 2. Llamamos al servicio que ahora busca en SQL Server
        String email = this.service.checkToken(token);

        // 3. Si el servicio devuelve null, el token no existe o ha expirado
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        // 4. Devolvemos el email del usuario (que sirve como identificador para
        // esientradas)
        return email;
    }

    /**
     * nombre_metodo: sendEmailWithPdf
     * parametros: request
     * funcion: envía email con PDF adjunto
     * flujo_en_el_que_participa: envío de correos
     */
    @PostMapping("/sendEmailWithPdf")
    public void sendEmailWithPdf(@Valid @RequestBody EmailRequest request) throws MessagingException {
        byte[] pdfBytes = Base64.getDecoder().decode(request.getPdfBase64());

        String html = emailTemplateService.generateEmail(
                request.getEmail(),
                "data:image/png;base64,");

        this.gmailService.sendHtmlEmailWithAttachment(
                request.getEmail(),
                "¡Tus Entradas!",
                html,
                pdfBytes);

        // Llamamos al servicio de email (ahora en esiusuarios)
        // this.emailService.sendEmail(request.getEmail(), "Tus Entradas", pdfBytes);
    }

    /**
     * nombre_metodo: procesarCompra
     * parametros: request
     * funcion: procesa compra completa (factura, QR, email)
     * flujo_en_el_que_participa: procesamiento de compras
     */
    /**
     * Procesa una compra completa (genera factura, QR, y envía por correo)
     * Endpoint delegado desde esientradas a través del mediador
     * 
     * @param request ProcesarCompraRequest con email y datos de entradas
     * @return String PDF en Base64
     */
    @PostMapping("/procesarCompra")
    public String procesarCompra(@Valid @RequestBody ProcesarCompraRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email es requerido");
        }

        if (request.getEntradas() == null || request.getEntradas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las entradas son requeridas");
        }

        try {
            // Convertir EntradaData a Map<String, String> para el mediador
            java.util.List<java.util.Map<String, String>> entradasMap = request.getEntradas().stream()
                    .map(entrada -> {
                        java.util.Map<String, String> map = new java.util.HashMap<>();
                        map.put("id", entrada.getId().toString());
                        map.put("artista", entrada.getArtista());
                        map.put("precio", entrada.getPrecio().toString());
                        return map;
                    })
                    .toList();

            // Delegar al mediador para procesar la compra completa
            byte[] pdf = mediadorService.procesarCompraCompleta(request.getEmail(), entradasMap);

            // Retornar el PDF en Base64 para que esientradas lo almacene localmente
            return Base64.getEncoder().encodeToString(pdf);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al procesar compra: " + e.getMessage());
        }
    }

    /**
     * Verifica si un usuario es ADMIN basado en su token
     * 
     * @param token Token del usuario
     * @return Boolean true si es ADMIN, false en caso contrario
     */
    @GetMapping("/checkAdmin/{token}")
    public Boolean checkAdmin(@PathVariable String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }

        String email = this.service.checkToken(token);
        if (email == null) {
            return false;
        }

        return this.service.esAdmin(email);
    }

    /**
     * Obtiene el rol de un usuario basado en su token
     * 
     * @param token Token del usuario
     * @return String El rol del usuario (USER, ADMIN, etc)
     */
    @GetMapping("/getRol/{token}")
    public String getRol(@PathVariable String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }

        String email = this.service.checkToken(token);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        return String.valueOf(this.service.obtenerRol(email));
    }

}