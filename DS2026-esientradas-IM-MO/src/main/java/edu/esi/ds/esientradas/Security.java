package edu.esi.ds.esientradas; // Cambiar el package según el proyecto (esiusuarios o esientradas)

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

public class Security {

    @ControllerAdvice
    public static class GlobalExceptionHandler {

        // Añadimos el logger para el registro interno (OWASP A09/A10)
        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
            logger.warn("[ERROR HTTP] {} - {}", ex.getStatusCode(), ex.getReason());

            HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
            Map<String, Object> response = new HashMap<>();
            response.put("status", status.value());

            if (status == HttpStatus.BAD_REQUEST) {
                response.put("error", "Solicitud no válida");
            } else if (status == HttpStatus.FORBIDDEN) {
                response.put("error", "Acceso denegado");
            } else {
                response.put("error", "Error en la solicitud");
            }

            response.put("timestamp", LocalDateTime.now());
            return new ResponseEntity<>(response, status);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
            // 1. Registro interno detallado para los administradores
            logger.error("⚠️ [EXCEPCIÓN CRÍTICA]: {}", ex.getMessage(), ex);

            // 2. Respuesta opaca y segura para el cliente (OWASP A10)
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Se ha producido un error interno en el servidor");
            response.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}