package edu.esi.ds.esiusuarios; // Cambiar el package según el proyecto (esiusuarios o esientradas)

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

public class Security {

    @ControllerAdvice
    public static class GlobalExceptionHandler {

        // Añadimos el logger para el registro interno (OWASP A09/A10)
        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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