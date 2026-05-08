package edu.esi.ds.esiusuarios.services;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;

@Service
public class LoginAttemptService {
    // Configuramos los límites según el estándar OWASP A07
    private final int MAX_ATTEMPTS = 5;
    private final int BLOCK_TIME_MINUTES = 15;

    // Mapas para guardar los intentos y los tiempos de bloqueo en memoria
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockTimeCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
        blockTimeCache.remove(key);
    }

    public void loginFailed(String key) {
        int attempts = attemptsCache.getOrDefault(key, 0);
        attempts++;
        attemptsCache.put(key, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            blockTimeCache.put(key, LocalDateTime.now().plusMinutes(BLOCK_TIME_MINUTES));
        }
    }

    public boolean isBlocked(String key) {
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
        return attemptsCache.getOrDefault(key, 0);
    }

    public LocalDateTime getBlockedUntil(String key) {
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
        LocalDateTime blockedUntil = getBlockedUntil(key);
        if (blockedUntil == null) {
            return 0L;
        }

        return Math.max(0L, Duration.between(LocalDateTime.now(), blockedUntil).getSeconds());
    }
}