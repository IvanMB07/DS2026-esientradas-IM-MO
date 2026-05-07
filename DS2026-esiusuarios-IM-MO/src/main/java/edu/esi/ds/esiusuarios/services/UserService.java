package edu.esi.ds.esiusuarios.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import edu.esi.ds.esiusuarios.dao.UserDao;
import edu.esi.ds.esiusuarios.model.User;
import edu.esi.ds.esiusuarios.model.UserRole;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
public class UserService implements CommandLineRunner {

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

    // --- GESTIÓN DE ROLES ---

    /**
     * Cambiar el rol de un usuario (solo para ADMIN)
     * 
     * @param adminEmail  Email del admin que realiza el cambio
     * @param adminToken  Token del admin para validación
     * @param targetEmail Email del usuario al que cambiar el rol
     * @param newRole     Nuevo rol (USER o ADMIN)
     * @return true si el cambio fue exitoso, false en caso contrario
     */
    @Transactional
    public boolean cambiarRol(String adminEmail, String adminToken, String targetEmail, String newRole) {
        // 1. Validar que el token del admin es correcto
        String adminValidated = checkToken(adminToken);
        if (adminValidated == null || !adminValidated.equals(adminEmail)) {
            logger.warn("⚠️ Intento de cambiar rol con token inválido: {}", adminEmail);
            return false;
        }

        // 2. Verificar que el admin es realmente ADMIN
        Optional<User> adminOpt = userDao.findByEmail(adminEmail);
        if (adminOpt.isEmpty() || adminOpt.get().getRole() != UserRole.ADMIN) {
            logger.warn("⚠️ Intento de cambiar rol sin permisos: {}", adminEmail);
            return false;
        }

        // 3. Buscar al usuario objetivo
        Optional<User> targetOpt = userDao.findByEmail(targetEmail);
        if (targetOpt.isEmpty()) {
            logger.warn("⚠️ Intento de cambiar rol de usuario inexistente: {}", targetEmail);
            return false;
        }

        // 4. Cambiar el rol
        try {
            UserRole role = UserRole.valueOf(newRole.toUpperCase());
            targetOpt.get().setRole(role);
            userDao.save(targetOpt.get());
            logger.info("✅ Rol de {} cambiado a {} por admin {}", targetEmail, role, adminEmail);
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Intento de asignar rol inválido: {}", newRole);
            return false;
        }
    }

    /**
     * Verificar si un usuario es ADMIN
     * 
     * @param email Email del usuario
     * @return true si es ADMIN, false en caso contrario
     */
    public boolean esAdmin(String email) {
        Optional<User> userOpt = userDao.findByEmail(email);
        return userOpt.isPresent() && userOpt.get().getRole() == UserRole.ADMIN;
    }

    /**
     * Obtener el rol de un usuario
     * 
     * @param email Email del usuario
     * @return El rol del usuario (USER o ADMIN), null si no existe
     */
    public UserRole obtenerRol(String email) {
        Optional<User> userOpt = userDao.findByEmail(email);
        return userOpt.map(user -> user.getRole() == null ? UserRole.USER : user.getRole()).orElse(null);
    }

    /**
     * Devuelve el perfil de un usuario autenticado.
     */
    public Optional<User> obtenerPerfil(String email, String rawToken) {
        if (rawToken == null) {
            return Optional.empty();
        }

        String hashedToken = hashToken(rawToken);
        Optional<User> userOpt = userDao.findByEmail(email);
        if (userOpt.isPresent() && hashedToken.equals(userOpt.get().getToken())) {
            return userOpt;
        }
        return Optional.empty();
    }

    /**
     * Cambia la contraseña de un usuario autenticado.
     */
    @Transactional
    public boolean cambiarPassword(String email, String rawToken, String currentPassword, String newPassword) {
        Optional<User> userOpt = obtenerPerfil(email, rawToken);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (!encoder.matches(currentPassword, user.getPassword())) {
            logger.warn("⚠️ Intento de cambio de contraseña con clave actual incorrecta: {}", email);
            return false;
        }

        user.setPassword(encoder.encode(newPassword));
        userDao.save(user);
        logger.info("Contraseña cambiada para: {}", email);
        return true;
    }

    /**
     * Elimina la cuenta de un usuario autenticado.
     */
    @Transactional
    public boolean eliminarCuentaPropia(String email, String rawToken) {
        Optional<User> userOpt = obtenerPerfil(email, rawToken);
        if (userOpt.isEmpty()) {
            return false;
        }

        userDao.deleteById(email);
        logger.info("Cuenta eliminada por el propio usuario: {}", email);
        return true;
    }

    /**
     * Lista todos los usuarios para el panel de administración.
     */
    public Iterable<User> listarUsuarios(String adminEmail, String adminToken) {
        String adminValidated = checkToken(adminToken);
        if (adminValidated == null || !adminValidated.equals(adminEmail)) {
            return null;
        }

        Optional<User> adminOpt = userDao.findByEmail(adminEmail);
        if (adminOpt.isEmpty() || adminOpt.get().getRole() != UserRole.ADMIN) {
            return null;
        }

        return userDao.findAll();
    }

    /**
     * Elimina a otro usuario. Solo ADMIN.
     */
    @Transactional
    public boolean eliminarUsuarioAdmin(String adminEmail, String adminToken, String targetEmail) {
        String adminValidated = checkToken(adminToken);
        if (adminValidated == null || !adminValidated.equals(adminEmail)) {
            logger.warn("⚠️ Intento de borrado admin con token inválido: {}", adminEmail);
            return false;
        }

        Optional<User> adminOpt = userDao.findByEmail(adminEmail);
        if (adminOpt.isEmpty() || adminOpt.get().getRole() != UserRole.ADMIN) {
            logger.warn("⚠️ Intento de borrado sin permisos: {}", adminEmail);
            return false;
        }

        if (!userDao.existsById(targetEmail)) {
            logger.warn("⚠️ Intento de borrar usuario inexistente: {}", targetEmail);
            return false;
        }

        userDao.deleteById(targetEmail);
        logger.info("Usuario eliminado por admin {}: {}", adminEmail, targetEmail);
        return true;
    }

    @Override
    public void run(String... args) {
        for (User user : userDao.findAll()) {
            if (user.getRole() == null) {
                user.setRole(UserRole.USER);
                userDao.save(user);
            }
        }
    }
}