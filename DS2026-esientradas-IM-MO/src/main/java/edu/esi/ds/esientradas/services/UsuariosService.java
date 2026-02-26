package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuariosService {

	private final RestTemplate restTemplate;
	private final String usuariosServiceUrl;

	public UsuariosService(@Value("${usuarios.service.url:http://localhost:8081}") String usuariosServiceUrl) {
		this.restTemplate = new RestTemplate();
		this.usuariosServiceUrl = usuariosServiceUrl;
	}

	public String checkToken(String token) {
		if (token == null || token.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token requerido");
		}

		String url = this.usuariosServiceUrl + "/external/checkToken/{token}";
		try {
			return this.restTemplate.getForObject(url, String.class, token);
		} catch (HttpClientErrorException.BadRequest e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido");
		} catch (HttpClientErrorException.Unauthorized e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
		} catch (RestClientException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se puede conectar con el servicio de usuarios");
		}
	}

}
