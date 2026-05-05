package edu.esi.ds.esientradas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
public class EsientradasApplication {

	public static void main(String[] args) {
		SpringApplication.run(EsientradasApplication.class, args);
	}

	// 🔒 CONFIGURACIÓN GLOBAL DE SEGURIDAD (CORS)
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**") // Se aplica a todos los endpoints
						.allowedOrigins("http://localhost:4200") // Solo vuestro frontend Angular
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true); // Necesario si enviáis cookies o cabeceras de auth
			}
		};
	}
}