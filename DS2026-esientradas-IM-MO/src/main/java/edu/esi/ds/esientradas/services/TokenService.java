package edu.esi.ds.esientradas.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import edu.esi.ds.esientradas.dao.EntradaDao; // Necesitamos esto para guardar cambios
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Token;

@Service
public class TokenService {

    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private EntradaDao entradaDao; // Inyectamos el DAO de entradas

    // Tiempo de expiración (10 minutos como pediste: 10 * 60 * 1000)
    private static final long EXPIRATION_TIME_MILLIS = 10 * 60 * 1000;

    @Transactional
    public void liberarEntradasExpiredToken(Token token) {
        if (token != null && token.getEntradas() != null) {
            System.out.println("[TOKEN SERVICE] Expirado token: " + token.getValor() + ". Liberando entradas...");

            for (Entrada entrada : token.getEntradas()) {
                if (entrada.getEstado() == Estado.RESERVADA) {
                    entrada.setEstado(Estado.DISPONIBLE);
                    entradaDao.save(entrada); // Aseguramos que el cambio se guarde en MySQL
                }
            }

            // Limpiamos y borramos el token
            token.getEntradas().clear();
            tokenDao.delete(token);
        }
    }

    @Scheduled(fixedRate = 60000) // Se ejecuta cada minuto
    public void liberarEntradasReservadas() {
        // Calculamos el tiempo límite de forma eficiente
        long tiempoLimite = System.currentTimeMillis() - EXPIRATION_TIME_MILLIS;

        // OPTIMIZACIÓN: Usamos el método que añadimos al TokenDao[cite: 3]
        List<Token> tokensCaducados = tokenDao.findByHoraBefore(tiempoLimite);

        for (Token token : tokensCaducados) {
            liberarEntradasExpiredToken(token);
        }
    }
}