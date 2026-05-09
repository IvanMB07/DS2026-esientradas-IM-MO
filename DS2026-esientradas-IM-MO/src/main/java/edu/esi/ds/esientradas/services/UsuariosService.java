package edu.esi.ds.esientradas.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuariosService {

	/**
	 * nombre_metodo: checkToken
	 * parametros: token
	 * funcion: valida token de sesion contra el microservicio de usuarios y
	 * devuelve email
	 * flujo_en_el_que_participa: autenticacion backend en operaciones de
	 * reserva/compra
	 * comunicacion: HTTP GET a esiusuarios /external/checkToken/{token}
	 */
	public String checkToken(String token) {
		// Asegurarse de que el puerto es el 8081 de esiusuarios
		String endpoint = "http://localhost:8081/external/checkToken";
		RestTemplate restTemplate = new RestTemplate();
		try {
			// Coincide con el @GetMapping("/checkToken/{token}") que hicimos en esiusuarios
			String email = restTemplate.getForObject(endpoint + "/" + token, String.class);
			if (email == null || email.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido");
			}
			return email;
		} catch (RestClientException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Error de comunicación con esiusuarios");
		}
	}

	/**
	 * nombre_metodo: isAdmin
	 * parametros: userToken
	 * funcion: verifica si el token pertenece a un usuario con rol administrador
	 * flujo_en_el_que_participa: autorizacion de endpoints de administracion
	 * comunicacion: HTTP GET a esiusuarios /external/checkAdmin/{token}
	 */
	public boolean isAdmin(String userToken) {
		String endpoint = "http://localhost:8081/external/checkAdmin";
		RestTemplate restTemplate = new RestTemplate();
		try {
			Boolean isAdmin = restTemplate.getForObject(endpoint + "/" + userToken, Boolean.class);
			return isAdmin != null && isAdmin;
		} catch (RestClientException e) {
			return false;
		}
	}

	/**
	 * nombre_metodo: getRol
	 * parametros: userToken
	 * funcion: obtiene el rol textual asociado a un token de usuario
	 * flujo_en_el_que_participa: decisiones de autorizacion basadas en rol
	 * comunicacion: HTTP GET a esiusuarios /external/getRol/{token}
	 */
	public String getRol(String userToken) {
		String endpoint = "http://localhost:8081/external/getRol";
		RestTemplate restTemplate = new RestTemplate();
		try {
			String role = restTemplate.getForObject(endpoint + "/" + userToken, String.class);
			return role;
		} catch (RestClientException e) {
			return null;
		}
	}

	/**
	 * nombre_metodo: enviarPdfAExterno
	 * parametros: name, email, pdfBytes
	 * funcion: envia un PDF en Base64 al microservicio de usuarios para envio por
	 * correo
	 * flujo_en_el_que_participa: notificacion post-compra
	 * comunicacion: HTTP POST a esiusuarios /external/sendEmailWithPdf
	 */
	public void enviarPdfAExterno(String name, String email, byte[] pdfBytes) {
		String endpoint = "http://localhost:8081/external/sendEmailWithPdf";
		RestTemplate restTemplate = new RestTemplate();

		// Preparamos el cuerpo con el PDF en Base64
		Map<String, String> body = new HashMap<>();
		body.put("email", email);
		body.put("pdfBase64", java.util.Base64.getEncoder().encodeToString(pdfBytes));

		restTemplate.postForObject(endpoint, body, String.class);
	}

	/**
	 * nombre_metodo: procesarCompraEnMediador
	 * parametros: email, entradasData
	 * funcion: delega la post-compra al mediador de usuarios y devuelve PDF en
	 * binario
	 * flujo_en_el_que_participa: tramo inter-servicios despues de confirmar pago
	 * comunicacion: HTTP POST a esiusuarios /external/procesarCompra
	 */
	public byte[] procesarCompraEnMediador(String email, List<Map<String, String>> entradasData) {
		String endpoint = "http://localhost:8081/external/procesarCompra";
		RestTemplate restTemplate = new RestTemplate();

		try {
			// Preparar solicitud con email y datos de entradas
			Map<String, Object> body = new HashMap<>();
			body.put("email", email);
			body.put("entradas", entradasData);

			// Llamar al mediador y recibir PDF en Base64
			String pdfBase64 = restTemplate.postForObject(endpoint, body, String.class);

			if (pdfBase64 == null || pdfBase64.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"El mediador no retornó PDF");
			}

			// Decodificar Base64 a bytes
			return java.util.Base64.getDecoder().decode(pdfBase64);
		} catch (RestClientException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Error de comunicación con el mediador en esiusuarios: " + e.getMessage());

		}

	}
}
