package edu.esi.ds.esiusuarios.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import edu.esi.ds.esiusuarios.dao.UserDao;
import edu.esi.ds.esiusuarios.model.User;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private EmailServicePasswordRecovery emailServicePasswordRecovery;

    @Autowired
    private LoginAttemptService loginAttemptService; // Sistema de Rate Limiting (A07)

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\\S+$).{8,}$";

    // --- MÉTODOS DE SESIÓN ---

    @Transactional
    public String login(String email, String password) {
        // [A07] Control de fuerza bruta
        if (loginAttemptService.isBlocked(email)) {
            logger.warn("⚠️ Login bloqueado por seguridad: {}", email);
            return null;
        }

        Optional<User> uOpt = userDao.findByEmail(email);

        if (uOpt.isPresent()) {
            User user = uOpt.get();
            if (encoder.matches(password, user.getPassword())) {
                loginAttemptService.loginSucceeded(email); // Reset tras éxito
                String rawToken = UUID.randomUUID().toString();
                user.setToken(hashToken(rawToken));
                userDao.save(user);
                logger.info("Login exitoso para: {}", email);
                return rawToken;
            } else {
                loginAttemptService.loginFailed(email); // Incremento tras fallo
                logger.warn("Fallo de autenticación: Contraseña incorrecta para: {}", email);
            }
        } else {
            loginAttemptService.loginFailed(email); // Incremento aunque no exista (evita enumeración)
            logger.warn("Fallo de autenticación: Usuario no registrado: {}", email);
        }
        return null;
    }

    // --- RECUPERACIÓN DE CONTRASEÑA ---

    @Transactional
    public void solicitarRecuperacion(String email) {
        // 1. [A07] Comprobamos si ya está bloqueado (por logins o peticiones previas)
        if (loginAttemptService.isBlocked(email)) {
            logger.warn("⚠️ Solicitud de recuperación bloqueada por abuso: {}", email);
            return;
        }

        // 2. [CRUCIAL] Registramos la petición como un "intento" para este email.
        // Lo hacemos AQUÍ arriba para que cuente tanto si el usuario existe como si no.
        loginAttemptService.loginFailed(email);

        Optional<User> uOpt = userDao.findByEmail(email);
        if (uOpt.isPresent()) {
            User user = uOpt.get();
            String rawToken = UUID.randomUUID().toString();

            user.setPwdRecoveryToken(hashToken(rawToken));
            user.setPwdRecoveryTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userDao.save(user);

            String cuerpo = "Tu código de recuperación es: " + rawToken + ". Caduca en 15 minutos.";
            emailServicePasswordRecovery.sendEmail(email, "Recuperación de contraseña", cuerpo);
            logger.info("Token de recuperación generado y enviado para: {}", email);
        } else {
            // Ya no hace falta llamar a loginFailed aquí porque ya lo hemos hecho arriba
            logger.warn("Solicitud de recuperación para email no registrado: {}", email);
        }
    }

    // --- RESTO DE MÉTODOS (IGUAL QUE LOS TENÍAS) ---

    @Transactional
    public String registrar(String email, String password) {
        if (email == null || !email.matches(EMAIL_PATTERN))
            throw new IllegalArgumentException("El formato del email no es válido.");

        if (password == null || !password.matches(PASSWORD_PATTERN))
            throw new IllegalArgumentException("La contraseña no cumple los requisitos de robustez.");

        if (userDao.existsById(email)) {
            logger.warn("Intento de registro fallido: El email {} ya existe.", email);
            return null;
        }

        String rawToken = UUID.randomUUID().toString();
        User newUser = new User(email.split("@")[0], email, encoder.encode(password));
        newUser.setToken(hashToken(rawToken));

        userDao.save(newUser);
        logger.info("Nuevo usuario registrado: {}", email);
        return rawToken;
    }

    public String checkToken(String rawToken) {
        if (rawToken == null)
            return null;
        String hashedToken = hashToken(rawToken);
        Optional<User> uOpt = userDao.findByToken(hashedToken);
        return uOpt.map(User::getEmail).orElse(null);
    }

    @Transactional
    public boolean resetearPassword(String rawToken, String newPassword) {
        if (rawToken == null)
            return false;
        String hashedToken = hashToken(rawToken);
        Optional<User> uOpt = userDao.findByPwdRecoveryToken(hashedToken);
        if (uOpt.isPresent()) {
            User user = uOpt.get();
            if (user.getPwdRecoveryTokenExpiry().isAfter(LocalDateTime.now())) {
                user.setPassword(encoder.encode(newPassword));
                user.setPwdRecoveryToken(null);
                user.setPwdRecoveryTokenExpiry(null);
                userDao.save(user);
                logger.info("Contraseña reseteada para: {}", user.getEmail());
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void cancelarCuenta(String email) {
        userDao.deleteById(email);
        logger.info("Cuenta eliminada: {}", email);
    }

    @Transactional
    public boolean logout(String email, String rawToken) {
        if (rawToken == null)
            return false;
        String hashedToken = hashToken(rawToken);
        Optional<User> uOpt = userDao.findByEmail(email);
        if (uOpt.isPresent() && hashedToken.equals(uOpt.get().getToken())) {
            uOpt.get().setToken(null);
            userDao.save(uOpt.get());
            logger.info("Logout para: {}", email);
            return true;
        }
        return false;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            logger.error("Error criptográfico");
            throw new RuntimeException("Error interno de seguridad", ex);
        }
    }
}