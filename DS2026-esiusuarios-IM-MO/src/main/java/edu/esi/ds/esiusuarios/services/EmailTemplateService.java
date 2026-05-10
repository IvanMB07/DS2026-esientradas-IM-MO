package edu.esi.ds.esiusuarios.services;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
/**
 * nombre_clase: EmailTemplateService
 * funcion: procesamiento de plantillas de correo electrónico con Thymeleaf
 * flujo_en_el_que_participa: generación de contenido de correos
 * comunicacion: TemplateEngine de Thymeleaf
 */
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    public EmailTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * nombre_metodo: generateEmail
     * parametros: nombre, qrBase64
     * funcion: genera contenido HTML del email a partir de plantilla
     * flujo_en_el_que_participa: generación de correos
     */
    public String generateEmail(String nombre, String qrBase64) {
        Context context = new Context();
        context.setVariable("nombre", nombre);
        context.setVariable("qrImage", qrBase64);

        return templateEngine.process("email", context);
    }
}
