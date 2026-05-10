package edu.esi.ds.esiusuarios.auxiliares;

import edu.esi.ds.esiusuarios.services.EmailService;
import edu.esi.ds.esiusuarios.services.EmailServiceFalso;

/**
 * nombre_clase: Manager
 * funcion: patrón Singleton para gestión de servicios de email
 * flujo_en_el_que_participa: inicialización de servicios
 * comunicacion: EmailService
 */
public class Manager {

    private static Manager yo;
    private final EmailService emailService;

    private Manager() {
        this.emailService = new EmailServiceFalso();
    }

    // El synchronized es para evitar problemas de concurrencia en entornos
    // multihilo
    /**
     * nombre_metodo: getInstance
     * parametros: ninguno
     * funcion: obtiene la instancia única del Manager
     * flujo_en_el_que_participa: inicialización
     */
    public synchronized static Manager getInstance() {
        if (yo == null) {
            yo = new Manager();
        }
        return yo;
    }

    /**
     * nombre_metodo: getEmailService
     * parametros: ninguno
     * funcion: obtiene el servicio de email
     * flujo_en_el_que_participa: envío de correos
     */
    public EmailService getEmailService() {
        return this.emailService;
    }

}
