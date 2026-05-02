package edu.esi.ds.esientradas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Token;

@Service
public class TokenService {

    @Autowired
    private TokenDao tokenDao;

    // Ajustado a 15 minutos según tu requerimiento (15 * 60 * 1000)
    private static final long EXPIRATION_TIME_MILLIS = 15 * 60 * 1000;

    @jakarta.transaction.Transactional // Asegura que si algo falla, no se borre el token a medias
    public void liberarEntradasExpiredToken(Token token) {
        if (token != null && token.getEntradas() != null) {
            System.out.println("[TOKEN SERVICE] Liberando " + token.getEntradas().size() + " entradas...");

            for (Entrada entrada : token.getEntradas()) {
                // Verificamos RESERVADA (según tu modelo)
                if (entrada.getEstado() == Estado.RESERVADA) {
                    entrada.setEstado(Estado.DISPONIBLE);
                    // Importante: No necesitas hacer save(entrada) si tienes CascadeType.ALL
                    // o si la entidad está gestionada por la transacción.
                }
            }

            // Limpiamos la lista de entradas del token antes de borrarlo para evitar
            // conflictos de FK
            token.getEntradas().clear();
            tokenDao.delete(token);
        }
    }

    @Scheduled(fixedRate = 60000) // Se ejecuta cada minuto
    public void liberarEntradasReservadas() {
        long ahora = System.currentTimeMillis();
        List<Token> todosLosTokens = tokenDao.findAll();

        for (Token token : todosLosTokens) {
            // El campo 'hora' viene de Token.java
            long tiempoDesdeCreacion = ahora - token.getHora();

            if (tiempoDesdeCreacion > EXPIRATION_TIME_MILLIS) {
                liberarEntradasExpiredToken(token);
            }
        }
    }
}