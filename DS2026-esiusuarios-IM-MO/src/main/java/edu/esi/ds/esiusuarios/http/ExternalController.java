package edu.esi.ds.esiusuarios.http;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esiusuarios.dto.EmailRequest;
import edu.esi.ds.esiusuarios.services.EmailService;
import edu.esi.ds.esiusuarios.services.EmailTemplateService;
import edu.esi.ds.esiusuarios.services.GmailService;
import edu.esi.ds.esiusuarios.services.UserService;
import jakarta.mail.MessagingException;

@CrossOrigin(origins = "http://localhost:4200") // Limitado al frontend Angular local.
@RestController
@RequestMapping("/external")
public class ExternalController {

    @Autowired
    private UserService service;

    @Autowired
    private GmailService gmailService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    // @Autowired
    // private EmailService emailService;

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

    @PostMapping("/sendEmailWithPdf")
    public void sendEmailWithPdf(@RequestBody EmailRequest request) throws MessagingException {
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

}