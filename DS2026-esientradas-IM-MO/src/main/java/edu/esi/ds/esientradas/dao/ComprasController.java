package edu.esi.ds.esientradas.http;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/compras")
public class ComprasController {

    @PostMapping("/comprar")
    public void comprar(@RequestBody Map<String, Object> payload) {
        Long idEntrada = ((Number) payload.get("idEntrada")).longValue();
        String token = (String) payload.get("token");

    }

    @PutMapping("/comprar2")
    public void comprar2(HttpSession session, HttpServletResponse response, @RequestParam String userToken) {
        String sessionId = session.getId();
        if (userToken == null || userToken.isEmpty()) {
            response.sendRedirect("https://www.uclm.es/");
            ;
            return;
        }

        this.usuariosService.checkToken(userToken);

    }

}
