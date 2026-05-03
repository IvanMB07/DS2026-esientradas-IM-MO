package edu.esi.ds.esiusuarios.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import edu.esi.ds.esiusuarios.dao.UserDao;
import edu.esi.ds.esiusuarios.model.User;
import java.util.Optional;
import java.util.UUID;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailServicePasswordRecovery emailServicePasswordRecovery;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String login(String email, String password) {
        Optional<User> uOpt = userDao.findByEmail(email);
        System.out.println("LOGIN: Buscando usuario: " + email);

        if (uOpt.isPresent()) {
            User user = uOpt.get();
            String storedPassword = user.getPassword();
            String storedToken = user.getToken();

            System.out.println("LOGIN: Usuario encontrado. Token en BD: "
                    + (storedToken != null ? storedToken.substring(0, 8) + "..." : "NULL"));
            System.out.println(
                    "LOGIN: Password en BD es plana: " + (storedPassword != null && !storedPassword.startsWith("$2")));

            // Intenta validar con BCrypt primero (para contraseñas encriptadas)
            if (storedPassword != null && (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$"))) {
                System.out.println("LOGIN: Validando con BCrypt");
                if (encoder.matches(password, storedPassword)) {
                    // Si el token es NULL o vacío, generar uno nuevo
                    if (storedToken == null || storedToken.isEmpty()) {
                        storedToken = UUID.randomUUID().toString();
                        user.setToken(storedToken);
                        userDao.save(user);
                        System.out.println("LOGIN: Token era NULL, generando uno nuevo");
                    }
                    System.out.println("LOGIN: ✓ BCrypt OK. Devolviendo token: " + storedToken);
                    return storedToken;
                }
            } else if (storedPassword != null && storedPassword.equals(password)) {
                System.out.println("LOGIN: Validando con contraseña plana");
                // FALLBACK: Contraseña plana coincide (legacy data o inserción manual)
                // Encriptamos y guardamos para futuras comparaciones
                user.setPassword(encoder.encode(password));
                // NO generar nuevo token, mantener el existente
                if (storedToken == null || storedToken.isEmpty()) {
                    user.setToken(UUID.randomUUID().toString());
                    System.out.println("LOGIN: Token era NULL, generando uno nuevo");
                }
                userDao.save(user);
                System.out.println("LOGIN: ✓ Contraseña plana OK. Devolviendo token: " + user.getToken());
                return user.getToken();
            } else {
                System.out.println("LOGIN: ✗ Contraseña NO coincide");
                System.out.println("LOGIN: Esperada: " + password + " | En BD: "
                        + (storedPassword != null ? storedPassword.substring(0, Math.min(20, storedPassword.length()))
                                : "NULL"));
            }
        } else {
            System.out.println("LOGIN: ✗ Usuario NO encontrado en BD");
        }
        return null;
    }

    // Cambiado a 'registrar' para coincidir con el Controller
    // 1. Añade esta constante al principio de la clase para definir la política
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{12,}$";

    public String registrar(String email, String password) {
        // 1. Validar Email
        if (email == null || !email.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("El formato del email no es válido.");
        }

        // 2. Validar Contraseña (ya lo tenemos del paso anterior)
        if (password == null || !password.matches(PASSWORD_PATTERN)) {
            throw new IllegalArgumentException("La contraseña no cumple los requisitos de robustez.");
        }

        if (userDao.existsById(email)) {
            return null;
        }

        String encodedPassword = encoder.encode(password);
        String defaultName = email.split("@")[0];

        User newUser = new User(defaultName, email, encodedPassword);
        String token = UUID.randomUUID().toString();
        newUser.setToken(token);

        userDao.save(newUser);
        return token;
    }

    public String checkToken(String token) {
        // Buscamos si existe algún usuario con ese token de sesión activo
        Optional<User> uOpt = userDao.findByToken(token);
        if (uOpt.isPresent()) {
            return uOpt.get().getEmail(); // Devolvemos el email del dueño del token
        }
        return null;
    }

    public void solicitarRecuperacion(String email) {
        Optional<User> uOpt = userDao.findByEmail(email);
        if (uOpt.isPresent()) {
            User user = uOpt.get();

            // 1. Generamos un token único de recuperación
            String token = UUID.randomUUID().toString();
            user.setPwdRecoveryToken(token);

            // 2. Ponemos fecha de caducidad (ej: 15 minutos desde ahora)
            user.setPwdRecoveryTokenExpiry(LocalDateTime.now().plusMinutes(15));

            userDao.save(user);

            // 3. Enviamos el "email" usando el servicio de recuperación de contraseña
            String cuerpo = "Tu token de recuperación es: " + token + ". Caduca en 15 minutos.";
            emailServicePasswordRecovery.sendEmail(email,
                    "Recuperación de contraseña",
                    cuerpo);
        }
    }

    public boolean resetearPassword(String token, String newPassword) {
        Optional<User> uOpt = userDao.findByPwdRecoveryToken(token);

        if (uOpt.isPresent()) {
            User user = uOpt.get();

            // SEGURIDAD: Comprobamos si el token ha caducado
            if (user.getPwdRecoveryTokenExpiry().isAfter(LocalDateTime.now())) {
                // Token válido y a tiempo: cambiamos la clave (encriptándola)
                user.setPassword(encoder.encode(newPassword));

                // Limpiamos el token para que no se pueda volver a usar
                user.setPwdRecoveryToken(null);
                user.setPwdRecoveryTokenExpiry(null);

                userDao.save(user);
                return true;
            }
        }
        return false;
    }

    public void cancelarCuenta(String email) {
        userDao.deleteById(email);
    }

    public boolean logout(String email, String token) {
        Optional<User> uOpt = userDao.findByEmail(email);
        if (uOpt.isPresent()) {
            User user = uOpt.get();
            if (user.getToken() != null && user.getToken().equals(token)) {
                user.setToken(null);
                userDao.save(user);
                return true;
            }
        }
        return false;
    }

}
