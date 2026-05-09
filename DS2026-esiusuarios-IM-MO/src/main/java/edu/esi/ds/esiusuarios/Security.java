package edu.esi.ds.esiusuarios;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.FieldError;

public class Security {

    @ControllerAdvice
    public static class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * CAPTURA: Errores de lectura de JSON (campos extra, JSON mal formado).
         * RESULTADO: 400 Bad Request (en lugar de 500).
         */
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> handleJsonErrors(HttpMessageNotReadableException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Solicitud mal formada o parámetros no permitidos");
            response.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        /**
         * CAPTURA: Fallos en las anotaciones de validación
         * (@Email, @Size, @NotBlank, @Pattern).
         * RESULTADO: 422 Unprocessable Entity.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
            response.put("error", "Los datos enviados no cumplen con el formato requerido");
            response.put("timestamp", LocalDateTime.now());

            List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> {
                        Map<String, String> error = new HashMap<>();
                        error.put("field", fieldError.getField());
                        error.put("message", fieldError.getDefaultMessage());
                        return error;
                    })
                    .collect(Collectors.toList());
            response.put("errors", errors);

            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        /**
         * CAPTURA: Errores controlados del negocio (ResponseStatusException).
         * RESULTADO: Preserva el código de estado y mensaje original.
         */
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", ex.getStatusCode().value());
            response.put("error", ex.getReason());
            response.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(response, ex.getStatusCode());
        }

        /**
         * CAPTURA: Cualquier otro error inesperado en el servidor.
         * RESULTADO: 500 Internal Server Error.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
            // Logeamos el error real para nosotros, pero no se lo damos al usuario
            logger.error("⚠️ [EXCEPCIÓN CRÍTICA]: {}", ex.getMessage(), ex);

            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Se ha producido un error interno en el servidor");
            response.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}