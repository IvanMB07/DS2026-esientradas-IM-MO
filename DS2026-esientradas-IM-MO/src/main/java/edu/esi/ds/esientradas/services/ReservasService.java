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
public class ReservasService {
    @Autowired
    private EntradaDao entradaDao;
    @Autowired
    private TokenDao tokenDao;

    @Transactional
    public String seleccionarEntrada(Long idEntrada, String compraToken) {
        Token token;
        if (compraToken == null || compraToken.isEmpty() || compraToken.equals("null")) {
            token = new Token();
        } else {
            token = this.tokenDao.findById(compraToken).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proceso de compra no encontrado"));
        }

        Entrada entrada = this.entradaDao.findById(idEntrada).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        if (entrada.getEstado() != Estado.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La entrada ya no está disponible");
        }

        // AGREGAR A LA LISTA
        token.addEntrada(entrada);
        this.tokenDao.save(token);
        this.entradaDao.updateEstado(idEntrada, Estado.RESERVADA);

        return token.getValor();
    }

    @Transactional
    public void cancelarEntrada(Long idEntrada, String compraToken) {
        Token token = this.tokenDao.findById(compraToken).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no válido"));

        Entrada entrada = this.entradaDao.findById(idEntrada).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        // Quitamos la entrada de la lista del token y liberamos el estado
        token.getEntradas().removeIf(e -> e.getId().equals(idEntrada));
        this.tokenDao.save(token);
        this.entradaDao.updateEstado(idEntrada, Estado.DISPONIBLE);
    }

    public Token getResumenCompra(String tokenValor) {
        return this.tokenDao.findById(tokenValor).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión de compra expirada"));
    }
}