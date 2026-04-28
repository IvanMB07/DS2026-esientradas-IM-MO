package edu.esi.ds.esiusuarios.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import edu.esi.ds.esiusuarios.dao.UserDao;
import edu.esi.ds.esiusuarios.model.User;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

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
        // En el futuro, aquí buscarás en la BD si el token es válido
        return "Usuario Validado";
    }
}