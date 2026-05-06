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
	 * Delega al mediador en esiusuarios el procesamiento completo de una compra
	 * (generación de PDF, QR y envío de correo)
	 * 
	 * @param email        Email del usuario
	 * @param entradasData Lista de datos de entradas
	 * @return byte[] PDF generado (en Base64 desde esiusuarios)
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
