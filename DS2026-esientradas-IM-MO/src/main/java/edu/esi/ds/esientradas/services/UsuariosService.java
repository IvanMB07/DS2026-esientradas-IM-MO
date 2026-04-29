package edu.esi.ds.esientradas.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuariosService {

	// Vuestro código está perfecto, solo sugerencia de limpieza:
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

}
