package edu.esi.ds.esiusuarios.http;

import java.util.Map;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import edu.esi.ds.esiusuarios.services.UserService;

@CrossOrigin(origins = "*") // Permitir CORS para todas las fuentes (ajusta según tus necesidades de
                            // seguridad)
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

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody Map<String, String> body) {
        String email = new JSONObject(body).optString("email");
        if (email.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        this.service.solicitarRecuperacion(email);
        // Por seguridad, siempre respondemos OK aunque el email no exista
    }

    @PostMapping("/reset-password")
    public void resetPassword(@RequestBody Map<String, String> body) {
        JSONObject jso = new JSONObject(body);
        String token = jso.optString("token");
        String newPwd = jso.optString("pwd");

        if (token.isEmpty() || newPwd.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        boolean exito = this.service.resetearPassword(token, newPwd);
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.GONE, "Token inválido o caducado");
        }
    }

    @DeleteMapping("/cancel")
    public void cancelar(@RequestParam String email, @RequestParam String token) {
        // Seguridad básica: comprobamos que el token pertenece al email que quiere
        // borrar
        String emailValidado = this.service.checkToken(token);
        if (emailValidado == null || !emailValidado.equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        this.service.cancelarCuenta(email);
    }

}