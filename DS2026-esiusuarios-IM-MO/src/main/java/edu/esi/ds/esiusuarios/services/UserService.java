package edu.esi.ds.esiusuarios.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import edu.esi.ds.esiusuarios.dao.UserDao;
import edu.esi.ds.esiusuarios.model.User;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailServicePasswordRecovery emailServicePasswordRecovery;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\\S+$).{8,}$";

    // --- MÉTODOS DE SESIÓN ---

    public String login(String email, String password) {
        Optional<User> uOpt = userDao.findByEmail(email);
        if (uOpt.isPresent()) {
            User user = uOpt.get();
            if (encoder.matches(password, user.getPassword())) {
                String rawToken = UUID.randomUUID().toString();
                user.setToken(hashToken(rawToken)); // Guardamos el HASH
                userDao.save(user);
                return rawToken; // Devolvemos el original al frontend
            }
        }
        return null;
    }

    public String registrar(String email, String password) {
        if (email == null || !email.matches(EMAIL_PATTERN))
            throw new IllegalArgumentException("Email inválido.");
        if (password == null || !password.matches(PASSWORD_PATTERN))
            throw new IllegalArgumentException("Contraseña débil.");

        if (userDao.existsById(email))
            return null;

        String rawToken = UUID.randomUUID().toString();
        User newUser = new User(email.split("@")[0], email, encoder.encode(password));
        newUser.setToken(hashToken(rawToken)); // Guardamos el HASH

        userDao.save(newUser);
        return rawToken;
    }

    public String checkToken(String rawToken) {
        if (rawToken == null)
            return null;
        String hashedToken = hashToken(rawToken);
        Optional<User> uOpt = userDao.findByToken(hashedToken);
        return uOpt.map(User::getEmail).orElse(null);
    }

    // --- RECUPERACIÓN DE CONTRASEÑA ---

    public void solicitarRecuperacion(String email) {
        Optional<User> uOpt = userDao.findByEmail(email);
        if (uOpt.isPresent()) {
            User user = uOpt.get();
            String rawToken = UUID.randomUUID().toString();

            user.setPwdRecoveryToken(hashToken(rawToken)); // Guardamos el HASH
            user.setPwdRecoveryTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userDao.save(user);

            String cuerpo = "Tu código de recuperación es: " + rawToken + ". Caduca en 15 minutos.";
            emailServicePasswordRecovery.sendEmail(email, "Recuperación de contraseña", cuerpo);
        }
    }

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
                return true;
            }
        }
        return false;
    }

    // --- OTROS MÉTODOS ---

    public void cancelarCuenta(String email) {
        userDao.deleteById(email);
    }

    public boolean logout(String email, String rawToken) {
        if (rawToken == null)
            return false;
        String hashedToken = hashToken(rawToken);
        Optional<User> uOpt = userDao.findByEmail(email);
        if (uOpt.isPresent() && hashedToken.equals(uOpt.get().getToken())) {
            uOpt.get().setToken(null);
            userDao.save(uOpt.get());
            return true;
        }
        return false;
    }

    /**
     * Helper para generar SHA-256 (A04:2025)
     */
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
            throw new RuntimeException("Error criptográfico", ex);
        }
    }
}