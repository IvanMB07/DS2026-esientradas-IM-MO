package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Token;
import jakarta.transaction.Transactional;

@Service
public class ComprasService {

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private UsuariosService usuariosService;

    @Transactional
    public String comprar(Long idEntrada, String compraToken, String userToken) {
        // 1. Verificar identidad del usuario (Token 1234 del diagrama)
        String userName = this.usuariosService.checkToken(userToken);

        // 2. Buscar la entrada
        Entrada entrada = this.entradaDao.findById(idEntrada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        // 3. Buscar el token de compra (Token abcd del diagrama)
        Token tokenReserva = this.tokenDao.findById(compraToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Token de compra no válido"));

        // 4. [CRÍTICO] Validar que la entrada pertenece a ESTE token
        // Esto sustituye al antiguo método del DAO
        if (!tokenReserva.getEntradas().contains(entrada)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta entrada no forma parte de tu reserva");
        }

        // 5. Finalizar proceso (Paso 33 del diagrama)
        this.entradaDao.updateEstado(idEntrada, Estado.VENDIDA);

        // Si quieres que el token desaparezca solo cuando se paguen TODAS,
        // podrías borrarlo aquí o al final del bucle en el controlador
        // this.tokenDao.delete(tokenReserva);

        return userName;
    }
}
