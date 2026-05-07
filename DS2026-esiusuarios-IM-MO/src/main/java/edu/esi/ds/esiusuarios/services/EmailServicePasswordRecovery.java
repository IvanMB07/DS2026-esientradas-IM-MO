package edu.esi.ds.esiusuarios.services;

import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;

@Service
public class EmailServicePasswordRecovery extends EmailService {

    private final GmailService gmailService;

    public EmailServicePasswordRecovery(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    /**
     * Envía email de recuperación de contraseña
     * Params esperados:
     * - params[0]: asunto del email (String)
     * - params[1]: cuerpo del email con el token (String)
     */
    @Override
    public void sendEmail(String destinatario, Object... params) {
        String asunto = (String) params[0];
        String cuerpo = params.length > 1 ? (String) params[1] : "";

        String htmlContent = "<html><body><h1>Recuperación de Contraseña</h1><p>" + cuerpo + "</p></body></html>";

        try {
            gmailService.sendHtmlEmail(destinatario, asunto, htmlContent);
            System.out.println("✅ Email de recuperación enviado exitosamente a: " + destinatario);
        } catch (MessagingException e) {
            System.err.println("❌ Error enviando email de recuperación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
