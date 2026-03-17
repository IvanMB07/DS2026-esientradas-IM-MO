package edu.esi.ds.esiusuarios.auxiliares;

import edu.esi.ds.esiusuarios.services.EmailService;
import edu.esi.ds.esiusuarios.services.EmailServiceFalso;

public class Manager {

    private static Manager yo;
    private final EmailService emailService;

    private Manager() {
        this.emailService = new EmailServiceFalso();
    }

    // El synchronized es para evitar problemas de concurrencia en entornos
    // multihilo
    public synchronized static Manager getInstance() {
        if (yo == null) {
            yo = new Manager();
        }
        return yo;
    }

    public EmailService getEmailService() {
        return this.emailService;
    }

}
