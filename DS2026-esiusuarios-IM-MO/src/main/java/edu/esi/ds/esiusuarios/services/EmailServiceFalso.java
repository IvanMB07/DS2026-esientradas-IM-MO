package edu.esi.ds.esiusuarios.services;

import org.springframework.stereotype.Service;

@Service
public class EmailServiceFalso extends EmailService {

    // edu.esi.ds.esiusuarios.services.EmailServiceFalso.java

    @Override
    public void sendEmail(String destinatario, Object... params) {
        String asunto = (String) params[0];

        System.out.println("--- LOG DE EMAIL (FALSO) ---");
        System.out.println("Para: " + destinatario);
        System.out.println("Asunto: " + asunto);

        // Verificamos si el segundo parámetro es el PDF (bytes) o un texto
        if (params.length > 1) {
            if (params[1] instanceof byte[]) {
                byte[] pdf = (byte[]) params[1];
                System.out.println("Archivo adjunto: PDF detectado (" + pdf.length + " bytes)");
            } else if (params[1] instanceof String) {
                System.out.println("Cuerpo: " + params[1]);
            }
        }
        System.out.println("----------------------------");
    }
}
