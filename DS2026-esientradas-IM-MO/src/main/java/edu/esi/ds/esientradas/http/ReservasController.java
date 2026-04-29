package edu.esi.ds.esientradas.http;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.services.ReservasService;
import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/reservas")
public class ReservasController {

    @Autowired
    private ReservasService service;

    @PostMapping("/seleccionar")
    public String seleccionar(@RequestBody Map<String, String> payload) {
        Long idEntrada = Long.parseLong(payload.get("idEntrada"));
        String compraToken = payload.get("compraToken");

        return this.service.seleccionarEntrada(idEntrada, compraToken);
    }

    @PostMapping("/cancelar")
    public void cancelar(@RequestBody Map<String, String> payload) {
        Long idEntrada = Long.parseLong(payload.get("idEntrada"));
        String compraToken = payload.get("compraToken");

        this.service.cancelarEntrada(idEntrada, compraToken);
    }

    @GetMapping("/resumen")
    public Object getResumen(@RequestParam String compraToken) {
        return this.service.getResumenCompra(compraToken);
    }
}
