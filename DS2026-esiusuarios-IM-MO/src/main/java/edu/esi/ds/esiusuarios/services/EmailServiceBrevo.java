package edu.esi.ds.esiusuarios.services;

import java.util.Collections;

import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailAttachment;
import brevoModel.SendSmtpEmailTo;

public class EmailServiceBrevo extends EmailService {

    @Override
    public void sendEmail(String destinatario, Object... params) {
        String asunto = (String) params[0];
        byte[] pdfBytes = (byte[]) params[1];

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSubject(asunto);
        sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(destinatario)));
        sendSmtpEmail.setHtmlContent("<html><body><h1>¡Aquí tienes tus entradas!</h1></body></html>");

        SendSmtpEmailAttachment attach = new SendSmtpEmailAttachment();
        attach.setContent(pdfBytes);
        attach.setName("Factura_ESIENTRADAS.pdf");
        sendSmtpEmail.setAttachment(Collections.singletonList(attach));

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
