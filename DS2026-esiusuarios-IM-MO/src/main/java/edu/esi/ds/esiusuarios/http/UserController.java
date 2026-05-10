package edu.esi.ds.esiusuarios.http;

import java.util.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import edu.esi.ds.esiusuarios.services.UserService;
import edu.esi.ds.esiusuarios.services.LoginAttemptService;
import edu.esi.ds.esiusuarios.dto.*;
import edu.esi.ds.esiusuarios.model.User;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
/**
 * nombre_clase: UserController
 * funcion: control de endpoints para gestión de usuarios
 * flujo_en_el_que_participa: registro, login, recuperación de contraseña
 * comunicacion: frontend Angular
 */
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requerido");
        }

        if (loginAttemptService.isBlocked(request.getEmail())) {
            return blockedResponse(request.getEmail(), "Cuenta bloqueada temporalmente");
        }

        String token = this.service.login(request.getEmail(), request.getPwd());
        if (token == null) {
            if (loginAttemptService.isBlocked(request.getEmail())) {
                return blockedResponse(request.getEmail(), "Cuenta bloqueada temporalmente");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildFailureResponse(request.getEmail(), "Email o contraseña incorrectos"));
        }
        return ResponseEntity.ok(token);
    }

    /**
     * nombre_metodo: registrar
     * parametros: request
     * funcion: registra un nuevo usuario
     * flujo_en_el_que_participa: registro
     */
    @PostMapping("/register")
    public ResponseEntity<String> registrar(@Valid @RequestBody RegisterRequest request) {
        if (!request.getPwd1().equals(request.getPwd2())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }

        try {
            String token = this.service.registrar(request.getEmail(), request.getPwd1());
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
            }
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    /**
     * nombre_metodo: logout
     * parametros: request
     * funcion: invalida la sesión del usuario
     * flujo_en_el_que_participa: logout
     */
    @PostMapping("/logout")
    public void logout(@Valid @RequestBody AuthRequest request) {
        boolean exito = this.service.logout(request.getEmail(), request.getToken());
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
    }

    /**
     * nombre_metodo: forgotPassword
     * parametros: request
     * funcion: solicita recuperación de contraseña
     * flujo_en_el_que_participa: recuperación de contraseña
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody EmailRequest request) {
        if (loginAttemptService.isBlocked(request.getEmail())) {
            return blockedResponse(request.getEmail(), "Demasiadas solicitudes");
        }
        this.service.solicitarRecuperacion(request.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * nombre_metodo: loginStatus
     * parametros: email
     * funcion: obtiene estado de bloqueo de cuenta por intentos fallidos
     * flujo_en_el_que_participa: validación de seguridad
     */
    @GetMapping("/login-status")
    public Map<String, Object> loginStatus(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requerido");
        }
        return buildBlockStatus(email);
    }

    /**
     * nombre_metodo: resetPassword
     * parametros: request, origin, referer
     * funcion: resetea la contraseña usando token de recuperación
     * flujo_en_el_que_participa: recuperación de contraseña
     */
    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request,
            @RequestHeader(value = "Origin", required = false) String origin,
            @RequestHeader(value = "Referer", required = false) String referer) {
        if (!isTrustedFrontendRequest(origin, referer)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Origen no permitido para resetear la contraseña");
        }

        boolean exito = this.service.resetearPassword(request.getToken(), request.getPwd());
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.GONE, "Token inválido o caducado");
        }
    }

    /**
     * nombre_metodo: cancelar
     * parametros: request
     * funcion: cancela la cuenta del usuario
     * flujo_en_el_que_participa: eliminación de cuenta
     */
    @DeleteMapping("/cancel")
    public void cancelar(@Valid @RequestBody AuthRequest request) {
        String emailValidado = this.service.checkToken(request.getToken());
        if (emailValidado == null || !emailValidado.equals(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        this.service.cancelarCuenta(request.getEmail());
    }

    /**
     * nombre_metodo: perfil
     * parametros: request
     * funcion: obtiene el perfil del usuario autenticado
     * flujo_en_el_que_participa: gestión de perfil
     */
    @PostMapping("/profile")
    public Map<String, String> perfil(@Valid @RequestBody AuthRequest request) {
        var perfil = this.service.obtenerPerfil(request.getEmail(), request.getToken());
        if (perfil.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        Map<String, String> response = new HashMap<>();
        response.put("email", perfil.get().getEmail());
        response.put("name", perfil.get().getName());
        response.put("role", String.valueOf(perfil.get().getRole()));
        return response;
    }

    /**
     * nombre_metodo: cambiarPassword
     * parametros: request
     * funcion: cambia la contraseña del usuario
     * flujo_en_el_que_participa: gestión de contraseña
     */
    @PostMapping("/profile/change-password")
    public void cambiarPassword(@Valid @RequestBody ChangePasswordRequest request) {
        boolean exito = this.service.cambiarPassword(request.getEmail(), request.getToken(),
                request.getCurrentPwd(), request.getNewPwd());
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se ha podido cambiar la contraseña");
        }
    }

    /**
     * nombre_metodo: eliminarCuentaPropia
     * parametros: request
     * funcion: elimina la cuenta propia del usuario
     * flujo_en_el_que_participa: eliminación de cuenta
     */
    @PostMapping("/profile/delete-account")
    public void eliminarCuentaPropia(@Valid @RequestBody AuthRequest request) {
        boolean exito = this.service.eliminarCuentaPropia(request.getEmail(), request.getToken());
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se ha podido eliminar la cuenta");
        }
    }

    /**
     * nombre_metodo: cambiarRol
     * parametros: request
     * funcion: cambia el rol de un usuario por un administrador
     * flujo_en_el_que_participa: gestión de roles
     */
    // --- GESTIÓN DE ROLES (ADMIN) ---

    @PostMapping("/change-role")
    public Map<String, String> cambiarRol(@Valid @RequestBody AdminRequest request) {
        if (request.getAdminEmail() != null && request.getTargetEmail() != null
                && request.getAdminEmail().trim().equalsIgnoreCase(request.getTargetEmail().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un administrador no puede cambiar su propio rol");
        }

        boolean exito = this.service.cambiarRol(request.getAdminEmail(), request.getAdminToken(),
                request.getTargetEmail(), request.getNewRole());
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado o el usuario no existe");
        }

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Rol cambiado exitosamente");
        response.put("email", request.getTargetEmail());
        response.put("nuevoRol", request.getNewRole());
        return response;
    }

    /**
     * nombre_metodo: obtenerRol
     * parametros: request
     * funcion: obtiene el rol del usuario por token
     * flujo_en_el_que_participa: gestión de roles
     */
    @PostMapping("/get-role")
    public Map<String, String> obtenerRol(@Valid @RequestBody TokenRequest request) {
        String token = request.getToken();
        String email = this.service.checkToken(token);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        response.put("role", String.valueOf(this.service.obtenerRol(email)));
        return response;
    }

    /**
     * nombre_metodo: listarUsuariosAdmin
     * parametros: request
     * funcion: lista todos los usuarios para administración
     * flujo_en_el_que_participa: administración
     */
    @PostMapping("/admin/users")
    public List<Map<String, String>> listarUsuariosAdmin(@Valid @RequestBody AdminAuthRequest request) {
        Iterable<User> usuarios = this.service.listarUsuarios(request.getAdminEmail(), request.getAdminToken());
        if (usuarios == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos de administrador");
        }

        List<Map<String, String>> response = new ArrayList<>();
        for (User usuario : usuarios) {
            Map<String, String> row = new HashMap<>();
            row.put("email", usuario.getEmail());
            row.put("name", usuario.getName());
            row.put("role", String.valueOf(usuario.getRole()));
            response.add(row);
        }
        return response;
    }

    /**
     * nombre_metodo: eliminarUsuarioAdmin
     * parametros: email, request
     * funcion: elimina un usuario por un administrador
     * flujo_en_el_que_participa: administración
     */
    @DeleteMapping("/admin/users/{email}")
    public void eliminarUsuarioAdmin(@PathVariable String email, @Valid @RequestBody AdminAuthRequest request) {
        boolean exito = this.service.eliminarUsuarioAdmin(request.getAdminEmail(), request.getAdminToken(), email);
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se ha podido eliminar el usuario");
        }
    }

    /**
     * nombre_metodo: blockedResponse
     * parametros: email, message
     * funcion: construye respuesta de cuenta bloqueada
     * flujo_en_el_que_participa: control de intentos fallidos
     */
    private ResponseEntity<Map<String, Object>> blockedResponse(String email, String message) {
        Map<String, Object> payload = buildBlockStatus(email);
        payload.put("message", message);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(payload.get("retryAfterSeconds")))
                .body(payload);
    }

    /**
     * nombre_metodo: buildFailureResponse
     * parametros: email, message
     * funcion: construye respuesta de fallo de login
     * flujo_en_el_que_participa: autenticación
     */
    private Map<String, Object> buildFailureResponse(String email, String message) {
        Map<String, Object> payload = buildBlockStatus(email);
        payload.put("message", message);
        return payload;
    }

    /**
     * nombre_metodo: buildBlockStatus
     * parametros: email
     * funcion: construye información de estado de bloqueo
     * flujo_en_el_que_participa: control de intentos
     */
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
     * nombre_metodo: isTrustedFrontendRequest
     * parametros: origin, referer
     * funcion: valida que la solicitud proceda del frontend autorizado
     * flujo_en_el_que_participa: validación de origen
     */
    private boolean isTrustedFrontendRequest(String origin, String referer) {
        String trustedOrigin = "http://localhost:4200";
        if (origin != null && origin.equalsIgnoreCase(trustedOrigin)) {
            return true;
        }
        return referer != null && referer.startsWith(trustedOrigin);
    }
}