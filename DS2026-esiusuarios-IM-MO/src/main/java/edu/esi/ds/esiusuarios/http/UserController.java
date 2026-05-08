package edu.esi.ds.esiusuarios.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import edu.esi.ds.esiusuarios.services.UserService;
import edu.esi.ds.esiusuarios.services.LoginAttemptService; // Importar el servicio de bloqueo

@CrossOrigin(origins = "http://localhost:4200") // Limitado al frontend Angular local.
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private LoginAttemptService loginAttemptService; // Inyectar el servicio de bloqueo

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        JSONObject jso = new JSONObject(credentials);
        String email = jso.optString("email");
        String password = jso.optString("pwd");

        // 1. [A07] Si está bloqueado, lanzamos 429 inmediatamente
        if (loginAttemptService.isBlocked(email)) {
            return blockedResponse(email, "Cuenta bloqueada temporalmente");
        }

        if (email.isEmpty() || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credenciales incompletas");
        }

        String token = this.service.login(email, password);
        if (token == null) {
            if (loginAttemptService.isBlocked(email)) {
                return blockedResponse(email, "Cuenta bloqueada temporalmente");
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
        }

        return ResponseEntity.ok(token);
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
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = new JSONObject(body).optString("email");

        // 1. [A07] Si está bloqueado, lanzamos 429
        if (loginAttemptService.isBlocked(email)) {
            return blockedResponse(email, "Demasiadas solicitudes");
        }

        if (email.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        this.service.solicitarRecuperacion(email);

        if (loginAttemptService.isBlocked(email)) {
            return blockedResponse(email, "Demasiadas solicitudes");
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/login-status")
    public Map<String, Object> loginStatus(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requerido");
        }

        return buildBlockStatus(email);
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

    private ResponseEntity<Map<String, Object>> blockedResponse(String email, String message) {
        Map<String, Object> payload = buildBlockStatus(email);
        payload.put("message", message);

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(payload.get("retryAfterSeconds")))
                .body(payload);
    }

    private Map<String, Object> buildBlockStatus(String email) {
        Map<String, Object> payload = new HashMap<>();
        boolean blocked = loginAttemptService.isBlocked(email);

        payload.put("email", email);
        payload.put("blocked", blocked);
        payload.put("attempts", loginAttemptService.getAttempts(email));
        payload.put("blockedUntil", loginAttemptService.getBlockedUntil(email));
        payload.put("retryAfterSeconds", loginAttemptService.getRemainingBlockSeconds(email));

        return payload;
    }

    /**
     * Perfil del usuario autenticado.
     */
    @PostMapping("/profile")
    public Map<String, String> perfil(@RequestBody Map<String, String> body) {
        JSONObject jso = new JSONObject(body);
        String email = jso.optString("email");
        String token = jso.optString("token");

        if (email.isEmpty() || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email o token ausente");
        }

        var perfil = this.service.obtenerPerfil(email, token);
        if (perfil.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        Map<String, String> response = new java.util.HashMap<>();
        response.put("email", perfil.get().getEmail());
        response.put("name", perfil.get().getName());
        response.put("role", String.valueOf(perfil.get().getRole()));
        return response;
    }

    /**
     * Cambiar contraseña desde el perfil.
     */
    @PostMapping("/profile/change-password")
    public void cambiarPassword(@RequestBody Map<String, String> body) {
        JSONObject jso = new JSONObject(body);
        String email = jso.optString("email");
        String token = jso.optString("token");
        String currentPassword = jso.optString("currentPwd");
        String newPassword = jso.optString("newPwd");

        if (email.isEmpty() || token.isEmpty() || currentPassword.isEmpty() || newPassword.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos");
        }

        boolean exito = this.service.cambiarPassword(email, token, currentPassword, newPassword);
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se ha podido cambiar la contraseña");
        }
    }

    /**
     * Baja de la cuenta desde el perfil.
     */
    @PostMapping("/profile/delete-account")
    public void eliminarCuentaPropia(@RequestBody Map<String, String> body) {
        JSONObject jso = new JSONObject(body);
        String email = jso.optString("email");
        String token = jso.optString("token");

        if (email.isEmpty() || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email o token ausente");
        }

        boolean exito = this.service.eliminarCuentaPropia(email, token);
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se ha podido eliminar la cuenta");
        }
    }

    // --- GESTIÓN DE ROLES ---

    /**
     * Endpoint para cambiar el rol de un usuario (solo ADMIN)
     * 
     * @param body Contiene: adminEmail, adminToken, targetEmail, newRole
     */
    @PostMapping("/change-role")
    public Map<String, String> cambiarRol(@RequestBody Map<String, String> body) {
        JSONObject jso = new JSONObject(body);
        String adminEmail = jso.optString("adminEmail");
        String adminToken = jso.optString("adminToken");
        String targetEmail = jso.optString("targetEmail");
        String newRole = jso.optString("newRole");

        if (adminEmail.isEmpty() || adminToken.isEmpty() || targetEmail.isEmpty() || newRole.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos");
        }

        boolean exito = this.service.cambiarRol(adminEmail, adminToken, targetEmail, newRole);
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes permisos para cambiar roles o el usuario no existe");
        }

        Map<String, String> response = new java.util.HashMap<>();
        response.put("mensaje", "Rol cambiado exitosamente");
        response.put("email", targetEmail);
        response.put("nuevoRol", newRole);
        return response;
    }

    /**
     * Endpoint para obtener el rol del usuario autenticado
     * 
     * @param token Token de autenticación
     * @return El rol del usuario (USER o ADMIN)
     */
    @PostMapping("/get-role")
    public Map<String, String> obtenerRol(@RequestBody Map<String, String> body) {
        JSONObject jso = new JSONObject(body);
        String token = jso.optString("token");

        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token requerido");
        }

        String email = this.service.checkToken(token);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        String rol = String.valueOf(this.service.obtenerRol(email));

        Map<String, String> response = new java.util.HashMap<>();
        response.put("email", email);
        response.put("role", rol);
        return response;
    }

    /**
     * Panel de administración: lista de usuarios.
     */
    @PostMapping("/admin/users")
    public List<Map<String, String>> listarUsuariosAdmin(@RequestBody Map<String, String> body) {
        JSONObject jso = new JSONObject(body);
        String adminEmail = jso.optString("adminEmail");
        String adminToken = jso.optString("adminToken");

        if (adminEmail.isEmpty() || adminToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos");
        }

        Iterable<?> usuarios = this.service.listarUsuarios(adminEmail, adminToken);
        if (usuarios == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador");
        }

        List<Map<String, String>> response = new ArrayList<>();
        for (Object usuarioObj : usuarios) {
            edu.esi.ds.esiusuarios.model.User usuario = (edu.esi.ds.esiusuarios.model.User) usuarioObj;
            Map<String, String> row = new HashMap<>();
            row.put("email", usuario.getEmail());
            row.put("name", usuario.getName());
            row.put("role", String.valueOf(usuario.getRole()));
            response.add(row);
        }
        return response;
    }

    /**
     * Panel de administración: eliminar usuario.
     */
    @DeleteMapping("/admin/users/{email}")
    public void eliminarUsuarioAdmin(@PathVariable String email, @RequestBody Map<String, String> body) {
        JSONObject jso = new JSONObject(body);
        String adminEmail = jso.optString("adminEmail");
        String adminToken = jso.optString("adminToken");

        if (adminEmail.isEmpty() || adminToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos incompletos");
        }

        boolean exito = this.service.eliminarUsuarioAdmin(adminEmail, adminToken, email);
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se ha podido eliminar el usuario");
        }
    }

}