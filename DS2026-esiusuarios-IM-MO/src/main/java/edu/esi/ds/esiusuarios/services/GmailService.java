package edu.esi.ds.esiusuarios.services;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;

@Service
/**
 * nombre_clase: GmailService
 * funcion: envío de correos electrónicos utilizando SMTP de Gmail
 * flujo_en_el_que_participa: envío de facturas y notificaciones
 * comunicacion: servidor SMTP de Gmail
 */
public class GmailService {

    private final String username;
    private final String appPassword;
    private final EmailTemplateService emailTemplateService;

    public GmailService(EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
        this.username = System.getenv("EMAIL_USER");
        this.appPassword = System.getenv("EMAIL_PWD");
    }

    /**
     * nombre_metodo: sendHtmlEmail
     * parametros: to, subject, htmlContent
     * funcion: envía email HTML usando Gmail SMTP
     * flujo_en_el_que_participa: notificaciones
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");
        message.setContent(htmlContent, "text/html; charset=UTF-8");

        Transport.send(message);
    }

    /**
     * nombre_metodo: sendHtmlEmailWithAttachment
     * parametros: to, subject, htmlContent, pdfBytes
     * funcion: envía email HTML con adjunto PDF usando Gmail SMTP
     * flujo_en_el_que_participa: envío de facturas
     */
    public void sendHtmlEmailWithAttachment(String to, String subject, String htmlContent, byte[] pdfBytes)
            throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");

        // Parte HTML
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");

        // Parte PDF
        MimeBodyPart pdfPart = new MimeBodyPart();
        ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
        pdfPart.setDataHandler(new DataHandler(dataSource));
        pdfPart.setFileName("entradas.pdf");

        // Unir ambas partes
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);
        multipart.addBodyPart(pdfPart);

        message.setContent(multipart);

        Transport.send(message);
    }

    /**
     * nombre_metodo: sendFacturaEmail
     * parametros: to, pdfBytes, qrBase64
     * funcion: envía email con factura PDF y QR opcional
     * flujo_en_el_que_participa: confirmación de compra
     */
    public void sendFacturaEmail(String to, byte[] pdfBytes, String qrBase64) throws MessagingException {
        String html = emailTemplateService.generateEmail(
                to,
                qrBase64 != null ? qrBase64 : "");

        sendHtmlEmailWithAttachment(
                to,
                "¡Tus Entradas!",
                html,
                pdfBytes);
    }
}