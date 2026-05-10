package edu.esi.ds.esiusuarios.services;

import org.springframework.stereotype.Service;

@Service
/**
 * nombre_clase: EmailServiceFalso
 * funcion: simulación de envío de correos para pruebas y desarrollo
 * flujo_en_el_que_participa: pruebas de notificaciones sin envío real
 * comunicacion: consola del sistema
 */
public class EmailServiceFalso extends EmailService {

    // edu.esi.ds.esiusuarios.services.EmailServiceFalso.java

    /**
     * nombre_metodo: sendEmail
     * parametros: destinatario, params
     * funcion: simula envío de email imprimiendo en consola
     * flujo_en_el_que_participa: pruebas
     */
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
