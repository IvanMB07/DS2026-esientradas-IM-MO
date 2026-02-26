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
    public String comprar(Long idEntrada, String sessionId, String userToken) {
        String userName = this.usuariosService.checkToken(userToken);

        Entrada entrada = this.entradaDao.findById(idEntrada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        if (entrada.getEstado() != Estado.RESERVADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La entrada no está reservada");
        }

        Token tokenReserva = this.tokenDao.findByEntradaIdAndSessionId(idEntrada, sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "La reserva no pertenece a esta sesión"));

        this.entradaDao.updateEstado(idEntrada, Estado.VENDIDA);
        this.tokenDao.delete(tokenReserva);

        return userName;
    }
}
