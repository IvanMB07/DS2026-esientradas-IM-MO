package edu.esi.ds.esientradas.http;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.services.UsuariosService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/compras")
// Clave: CORS habilitado solo para el frontend de la practica.
@CrossOrigin(origins = "http://localhost:4200")
public class ComprasController {

    @Autowired
    private UsuariosService usuariosService;

    /**
     * nombre_metodo: comprar
     * parametros: session, response, userToken
     * funcion: valida token de usuario y confirma identidad para continuar compra
     * flujo_en_el_que_participa: validacion de sesion en la fase de compra
     * comunicacion: UsuariosService.checkToken -> esiusuarios
     * /external/checkToken/{token}
     * nota_mantenimiento: endpoint legado; el cierre de compra activo se ejecuta en
     * /pagos/confirmar
     */
    @PutMapping("/comprar")
    public String comprar(HttpSession session, HttpServletResponse response, @RequestParam String userToken)
            throws IOException {
        String sessionId = session.getId();
        if (userToken == null || userToken.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token requerido");
        }

        // Validar que el token es legítimo
        String emailUsuario = this.usuariosService.checkToken(userToken);
        if (emailUsuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado");
        }

        return emailUsuario;
    }

}
