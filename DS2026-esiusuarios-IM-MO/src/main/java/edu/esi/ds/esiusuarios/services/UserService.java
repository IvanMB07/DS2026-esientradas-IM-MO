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

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String login(String email, String password) {
        Optional<User> uOpt = userDao.findByEmail(email);
        if (uOpt.isPresent()) {
            User user = uOpt.get();
            // Verifica la contraseña contra el hash de la BD
            if (encoder.matches(password, user.getPassword())) {
                return user.getToken();
            }
        }
        return null;
    }

    // Cambiado a 'registrar' para coincidir con el Controller
    public String registrar(String email, String password) {
        // SEGURIDAD: Verificamos si el usuario ya existe antes de crear otro
        if (userDao.existsById(email)) {
            return null;
        }

        String encodedPassword = encoder.encode(password);

        // Usamos el email como nombre por defecto si no nos pasan uno
        String defaultName = email.split("@")[0];

        User newUser = new User(defaultName, email, encodedPassword);
        newUser.setToken(UUID.randomUUID().toString());

        userDao.save(newUser);
        return newUser.getToken(); // Devolvemos el token para que el usuario ya esté "logueado"
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

            // 3. Enviamos el "email" (se verá en la consola de VS Code)
            emailService.sendEmail(email,
                    "Asunto", "Recuperación de contraseña",
                    "Cuerpo", "Tu token de recuperación es: " + token + ". Caduca en 15 minutos.");
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
}