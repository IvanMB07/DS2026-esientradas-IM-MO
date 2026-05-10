package edu.esi.ds.esiusuarios.services;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.Locale;

@Service
/**
 * nombre_clase: LoginAttemptService
 * funcion: control de intentos de login para prevenir ataques de fuerza bruta
 * flujo_en_el_que_participa: autenticación de usuarios
 * comunicacion: almacenamiento en memoria concurrente
 */
public class LoginAttemptService {
    // Configuramos los límites según el estándar OWASP A07
    private final int MAX_ATTEMPTS = 5;
    private final int BLOCK_TIME_MINUTES = 15;

    // Mapas para guardar los intentos y los tiempos de bloqueo en memoria
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockTimeCache = new ConcurrentHashMap<>();

    /**
     * nombre_metodo: normalizeKey
     * parametros: key
     * funcion: normaliza la clave para consistencia
     * flujo_en_el_que_participa: gestión de intentos
     */
    private String normalizeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * nombre_metodo: loginSucceeded
     * parametros: key
     * funcion: resetea los intentos tras login exitoso
     * flujo_en_el_que_participa: autenticación exitosa
     */
    public void loginSucceeded(String key) {
        key = normalizeKey(key);
        attemptsCache.remove(key);
        blockTimeCache.remove(key);
    }

    /**
     * nombre_metodo: loginFailed
     * parametros: key
     * funcion: incrementa intentos y bloquea si excede límite
     * flujo_en_el_que_participa: autenticación fallida
     */
    public void loginFailed(String key) {
        key = normalizeKey(key);
        int attempts = attemptsCache.getOrDefault(key, 0);
        attempts++;
        attemptsCache.put(key, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            blockTimeCache.put(key, LocalDateTime.now().plusMinutes(BLOCK_TIME_MINUTES));
        }
    }

    /**
     * nombre_metodo: isBlocked
     * parametros: key
     * funcion: verifica si la clave está bloqueada
     * flujo_en_el_que_participa: control de acceso
     */
    public boolean isBlocked(String key) {
        key = normalizeKey(key);
        if (blockTimeCache.containsKey(key)) {
            // Si el tiempo de bloqueo ya pasó, liberamos la cuenta automáticamente
            if (blockTimeCache.get(key).isBefore(LocalDateTime.now())) {
                loginSucceeded(key);
                return false;
            }
            return true;
        }
        return false;
    }

    public int getAttempts(String key) {
        key = normalizeKey(key);
        return attemptsCache.getOrDefault(key, 0);
    }

    public LocalDateTime getBlockedUntil(String key) {
        key = normalizeKey(key);
        if (!blockTimeCache.containsKey(key)) {
            return null;
        }

        LocalDateTime blockedUntil = blockTimeCache.get(key);
        if (blockedUntil.isBefore(LocalDateTime.now())) {
            loginSucceeded(key);
            return null;
        }

        return blockedUntil;
    }

    public long getRemainingBlockSeconds(String key) {
        key = normalizeKey(key);
        LocalDateTime blockedUntil = getBlockedUntil(key);
        if (blockedUntil == null) {
            return 0L;
        }

        return Math.max(0L, Duration.between(LocalDateTime.now(), blockedUntil).getSeconds());
    }
}