package edu.esi.ds.esiusuarios.services;

import org.springframework.stereotype.Service;

@Service
public class EmailServicePasswordRecovery extends EmailService {

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

        System.out.println("\n--- EMAIL DE RECUPERACIÓN DE CONTRASEÑA ---");
        System.out.println("Para: " + destinatario);
        System.out.println("Asunto: " + asunto);
        System.out.println("Cuerpo: " + cuerpo);
        System.out.println("----------------------------------------\n");
    }
}
