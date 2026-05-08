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
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        if (loginAttemptService.isBlocked(request.getEmail())) {
            return blockedResponse(request.getEmail(), "Cuenta bloqueada temporalmente");
        }

        String token = this.service.login(request.getEmail(), request.getPwd());
        if (token == null) {
            if (loginAttemptService.isBlocked(request.getEmail())) {
                return blockedResponse(request.getEmail(), "Cuenta bloqueada temporalmente");
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
        }
        return ResponseEntity.ok(token);
    }

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

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody AuthRequest request) {
        boolean exito = this.service.logout(request.getEmail(), request.getToken());
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody EmailRequest request) {
        if (loginAttemptService.isBlocked(request.getEmail())) {
            return blockedResponse(request.getEmail(), "Demasiadas solicitudes");
        }
        this.service.solicitarRecuperacion(request.getEmail());
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

    @DeleteMapping("/cancel")
    public void cancelar(@Valid @RequestBody AuthRequest request) {
        String emailValidado = this.service.checkToken(request.getToken());
        if (emailValidado == null || !emailValidado.equals(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        this.service.cancelarCuenta(request.getEmail());
    }

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

    @PostMapping("/profile/change-password")
    public void cambiarPassword(@Valid @RequestBody ChangePasswordRequest request) {
        boolean exito = this.service.cambiarPassword(request.getEmail(), request.getToken(),
                request.getCurrentPwd(), request.getNewPwd());
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se ha podido cambiar la contraseña");
        }
    }

    @PostMapping("/profile/delete-account")
    public void eliminarCuentaPropia(@Valid @RequestBody AuthRequest request) {
        boolean exito = this.service.eliminarCuentaPropia(request.getEmail(), request.getToken());
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se ha podido eliminar la cuenta");
        }
    }

    // --- GESTIÓN DE ROLES (ADMIN) ---

    @PostMapping("/change-role")
    public Map<String, String> cambiarRol(@Valid @RequestBody AdminRequest request) {
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

    @DeleteMapping("/admin/users/{email}")
    public void eliminarUsuarioAdmin(@PathVariable String email, @Valid @RequestBody AdminAuthRequest request) {
        boolean exito = this.service.eliminarUsuarioAdmin(request.getAdminEmail(), request.getAdminToken(), email);
        if (!exito) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se ha podido eliminar el usuario");
        }
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

    private boolean isTrustedFrontendRequest(String origin, String referer) {
        String trustedOrigin = "http://localhost:4200";
        if (origin != null && origin.equalsIgnoreCase(trustedOrigin)) {
            return true;
        }
        return referer != null && referer.startsWith(trustedOrigin);
    }
}