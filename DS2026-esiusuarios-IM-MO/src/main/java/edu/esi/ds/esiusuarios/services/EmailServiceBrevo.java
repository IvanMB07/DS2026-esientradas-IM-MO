package edu.esi.ds.esiusuarios.services;

import brevoApi.TransactionalEmailsApi;
import brevo.ApiException;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailAttachment;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Primary // <--- IMPORTANTE: Esto le dice a Spring que use este y NO el Falso
public class EmailServiceBrevo extends EmailService {

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

        // Sustituye por tu API Key real de Brevo
        String apiKey = "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";

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
        apiInstance.getApiClient().setApiKey(apiKey);

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