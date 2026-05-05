package edu.esi.ds.esiusuarios;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

public class Security {
    @ControllerAdvice
    public class GlobalExceptionHandler {

        // Atrapa cualquier error no controlado (ej. SQL fallando, NullPointer)
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            // Nunca enviar ex.getMessage() o ex.getStackTrace() al cliente
            response.put("error", "Se ha producido un error interno en el servidor");
            response.put("timestamp", LocalDateTime.now());

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
