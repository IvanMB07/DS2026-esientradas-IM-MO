package edu.esi.ds.esiusuarios.http;

import java.util.Map;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import edu.esi.ds.esiusuarios.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        JSONObject jso = new JSONObject(credentials);
        String email = jso.optString("email");
        String password = jso.optString("pwd");

        if (email.isEmpty() || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credenciales incompletas");
        }

        String token = this.service.login(email, password);
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
        }
        return token;
    }

    @PostMapping("/register")
    public String registrar(@RequestBody Map<String, String> credentials) {
        JSONObject jso = new JSONObject(credentials);
        String email = jso.optString("email");
        String pwd1 = jso.optString("pwd1");
        String pwd2 = jso.optString("pwd2");

        if (email.isEmpty() || pwd1.isEmpty() || pwd2.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos de registro incompletos");
        }

        // Validación de seguridad: las contraseñas deben ser iguales
        if (!pwd1.equals(pwd2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }

        String token = this.service.registrar(email, pwd1);

        // Si el servicio devuelve null es porque el email ya estaba en la BD
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        }

        return token;
    }
}