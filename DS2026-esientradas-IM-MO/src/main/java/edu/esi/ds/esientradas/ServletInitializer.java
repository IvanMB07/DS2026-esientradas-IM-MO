package edu.esi.ds.esientradas;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * nombre_clase: ServletInitializer
 * funcion: configuración para despliegue en servidor de aplicaciones
 * flujo_en_el_que_participa: inicialización en contenedor servlet
 * comunicacion: Spring Boot
 */
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EsientradasApplication.class);
	}

}
