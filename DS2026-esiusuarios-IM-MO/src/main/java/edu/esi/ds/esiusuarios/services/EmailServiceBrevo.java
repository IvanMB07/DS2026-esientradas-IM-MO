package edu.esi.ds.esiusuarios.services;

import brevoApi.TransactionalEmailsApi;
import brevo.ApiException;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailAttachment;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Primary // <--- IMPORTANTE: Esto le dice a Spring que use este y NO el Falso
/**
 * nombre_clase: EmailServiceBrevo
 * funcion: envío de correos electrónicos utilizando la API de Brevo
 * flujo_en_el_que_participa: notificaciones de registro, recuperación de
 * contraseña, envío de facturas
 * comunicacion: API de Brevo
 */
public class EmailServiceBrevo extends EmailService {

    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    /**
     * nombre_metodo: sendEmail
     * parametros: destinatario, params
     * funcion: envía email con adjunto PDF usando Brevo
     * flujo_en_el_que_participa: envío de facturas
     */
    @Override
    public void sendEmail(String destinatario, Object... params) {
        String asunto = (String) params[0];
        byte[] pdfBytes = (byte[]) params[1];

        // --- NUEVA LÍNEA PARA VERIFICACIÓN LOCAL ---
        try {
            // Guarda el PDF en la raíz del proyecto para que puedas abrirlo manualmente
            java.nio.file.Files.write(java.nio.file.Paths.get("prueba_debug.pdf"), pdfBytes);
            System.out.println("Archivo 'prueba_debug.pdf' guardado localmente para verificación.");
        } catch (Exception e) {
            System.err.println("No se pudo guardar el archivo localmente: " + e.getMessage());
        }
        // -------------------------------------------

        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            throw new IllegalStateException("brevo.api.key no configurada");
        }

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
        apiInstance.getApiClient().setApiKey(brevoApiKey);

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSubject(asunto);
        sendSmtpEmail.setHtmlContent(
                "<html><body><h1>¡Gracias por tu compra!</h1><p>Adjunto tienes tus entradas.</p></body></html>");
        sendSmtpEmail.setSender(new SendSmtpEmailSender().name("Ivan").email("ivanmoreno6700@gmail.com"));
        sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(destinatario)));

        // Adjuntamos el PDF
        SendSmtpEmailAttachment attach = new SendSmtpEmailAttachment();
        attach.setContent(pdfBytes);
        attach.setName("Tus_Entradas.pdf");
        sendSmtpEmail.setAttachment(Collections.singletonList(attach));

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("Correo REAL enviado con éxito a: " + destinatario);
        } catch (ApiException e) {
            System.err.println("Error enviando correo real. HTTP " + e.getCode());
            System.err.println("Respuesta Brevo: " + e.getResponseBody());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error enviando correo real: " + e);
            e.printStackTrace();
        }
    }
}