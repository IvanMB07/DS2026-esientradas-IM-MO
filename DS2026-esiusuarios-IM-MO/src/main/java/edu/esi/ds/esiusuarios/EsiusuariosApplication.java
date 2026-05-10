package edu.esi.ds.esiusuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
/**
 * nombre_clase: EsiusuariosApplication
 * funcion: punto de entrada principal de la aplicación Spring Boot para gestión
 * de usuarios
 * flujo_en_el_que_participa: inicialización de la aplicación
 * comunicacion: configuración de CORS y otros beans
 */
public class EsiusuariosApplication {

	public static void main(String[] args) {
		SpringApplication.run(EsiusuariosApplication.class, args);
	}

	/**
	 * nombre_metodo: corsConfigurer
	 * parametros: ninguno
	 * funcion: configura CORS para permitir solicitudes desde el frontend Angular
	 * flujo_en_el_que_participa: configuración de seguridad
	 */
	// 🔒 CONFIGURACIÓN GLOBAL DE SEGURIDAD (CORS)
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins("http://localhost:4200")
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
			}
		};
	}
}