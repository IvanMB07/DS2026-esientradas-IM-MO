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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import edu.esi.ds.esiusuarios.services.UserService;

@CrossOrigin(origins = "http://localhost:4200") // Limitado al frontend Angular local.
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos");
        }

        if (!pwd1.equals(pwd2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }

        try {
            String token = this.service.registrar(email, pwd1);
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
            }
            return token;
        } catch (IllegalArgumentException e) {
            // [Inferencia] Enviamos el mensaje específico (ej: "Formato de email inválido")
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @PostMapping("/logout")
    public void logout(@RequestBody Map<String, String> body) {
        JSONObject jso = new JSONObject(body);
        String email = jso.optString("email");
        String token = jso.optString("token");

        if (email.isEmpty() || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email o token ausente");
        }

        boolean exito = this.service.logout(email, token);
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
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
    public void resetPassword(@RequestBody Map<String, String> body,
            @RequestHeader(value = "Origin", required = false) String origin,
            @RequestHeader(value = "Referer", required = false) String referer) {
        if (!isTrustedFrontendRequest(origin, referer)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Origen no permitido para resetear la contraseña");
        }

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

    private boolean isTrustedFrontendRequest(String origin, String referer) {
        String trustedOrigin = "http://localhost:4200";
        if (origin != null && origin.equalsIgnoreCase(trustedOrigin)) {
            return true;
        }
        return referer != null && referer.startsWith(trustedOrigin);
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