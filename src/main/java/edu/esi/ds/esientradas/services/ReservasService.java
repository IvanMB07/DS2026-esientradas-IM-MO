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
    private EntradaDao dao;

    @Autowired
    private TokenDao tokenDao;

    @Transactional
    public Long reservar(Long idEntrada, String sessionId){
        Entrada entrada = this.dao.findById(idEntrada).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        if(entrada.getEstado() != Estado.DISPONIBLE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entrada no disponible");
        }
        //entrada.setEstado(Estado.RESERVADA);
        //this.dao.save(entrada);
        
        //entrada.setToken(new Token());
        
        Token token = new Token();
        token.setEntrada(entrada);
        token.setSessionId(sessionId);
        this.tokenDao.save(token);

        this.dao.updateEstado(idEntrada, Estado.RESERVADA);
        return entrada.getPrecio();
    }

}