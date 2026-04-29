package edu.esi.ds.esientradas.services;

import java.util.UUID;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private UsuariosService usuariosService;

    @Transactional
    public String seleccionarEntrada(Long idEntrada, String compraToken, String userToken) {
        String emailActual = null;
        if (userToken != null && !userToken.isEmpty() && !userToken.equals("null") && !userToken.equals("undefined")) {
            emailActual = usuariosService.checkToken(userToken);
        }

        Token token;
        if (compraToken == null || compraToken.isEmpty() || compraToken.equals("null")
                || compraToken.equals("undefined")) {
            token = new Token();
            token.setValor(UUID.randomUUID().toString());
            if (emailActual != null)
                token.setEmailUsuario(emailActual);
            // La fecha NO se asigna aquí, la pone MySQL por defecto
            this.tokenDao.save(token);
        } else {
            token = this.tokenDao.findById(compraToken).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proceso de compra no encontrado"));

            if (emailActual != null && token.getEmailUsuario() == null) {
                token.setEmailUsuario(emailActual);
                this.tokenDao.save(token);
            }
        }

        Entrada entrada = this.entradaDao.findById(idEntrada).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        if (entrada.getEstado() != Estado.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entrada ya reservada");
        }

        token.addEntrada(entrada);
        this.tokenDao.save(token);
        this.entradaDao.updateEstado(idEntrada, Estado.RESERVADA);

        return token.getValor();
    }

    @Transactional
    public void cancelarEntrada(Long idEntrada, String compraToken, String userToken) {
        Token token = this.tokenDao.findById(compraToken).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token no válido"));

        token.getEntradas().removeIf(e -> e.getId().equals(idEntrada));
        this.tokenDao.save(token);
        this.entradaDao.updateEstado(idEntrada, Estado.DISPONIBLE);
    }

    public Token getResumenCompra(String tokenValor, String userToken) {
        Token token = this.tokenDao.findById(tokenValor).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión expirada"));

        // Si hay userToken y el token no está vinculado a un usuario, vincularlo
        if (userToken != null && !userToken.isEmpty() && !userToken.equals("null")
                && !userToken.equals("undefined") && token.getEmailUsuario() == null) {
            String emailActual = usuariosService.checkToken(userToken);
            if (emailActual != null) {
                token.setEmailUsuario(emailActual);
                this.tokenDao.save(token);
            }
        }

        return token;
    }
}