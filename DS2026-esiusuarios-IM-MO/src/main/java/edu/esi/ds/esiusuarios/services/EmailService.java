package edu.esi.ds.esiusuarios.services;

/**
 * nombre_clase: EmailService
 * funcion: clase abstracta base para servicios de envío de correo
 * flujo_en_el_que_participa: envío de notificaciones por email
 * comunicacion: subclases especializadas (Gmail, Brevo, Falso)
 */
public abstract class EmailService {

    /**
     * nombre_metodo: sendEmail
     * parametros: destinatario, params
     * funcion: envía correo al destinatario
     * flujo_en_el_que_participa: envío de notificaciones
     */
    public abstract void sendEmail(String destinatario, Object... params);

}
