package edu.esi.ds.esientradas.http;

import java.util.Map;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/compras")
public class ComprasController {

    @PostMapping("/comprar")
    public void comprar(@RequestBody Map<String, Object> payload) {
        Long idEntrada = ((Number) payload.get("idEntrada")).longValue();
        String token = (String) payload.get("token");

    }

}
