package edu.esi.ds.esiusuarios.services;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public EmailTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generateEmail(String nombre, String qrBase64) {
        Context context = new Context();
        context.setVariable("nombre", nombre);
        context.setVariable("qrImage", qrBase64);

        return templateEngine.process("email", context);
    }
}
